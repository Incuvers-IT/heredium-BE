package art.heredium.service;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.domain.popup.entity.Popup;
import art.heredium.domain.popup.model.dto.request.GetAdminPopupRequest;
import art.heredium.domain.popup.model.dto.request.PostAdminPopupRequest;
import art.heredium.domain.popup.model.dto.request.PutAdminPopupOrderRequest;
import art.heredium.domain.popup.model.dto.response.GetAdminPopupDetailResponse;
import art.heredium.domain.popup.model.dto.response.GetAdminPopupResponse;
import art.heredium.domain.popup.repository.PopupRepository;
import art.heredium.ncloud.bean.CloudStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PopupService {

    private final PopupRepository popupRepository;
    private final LogRepository logRepository;
    private final CloudStorage cloudStorage;

    public Page<GetAdminPopupResponse> list(GetAdminPopupRequest dto, Pageable pageable) {
        return popupRepository.search(dto, pageable)
                .map(GetAdminPopupResponse::new);
    }

    public GetAdminPopupDetailResponse detailByAdmin(Long id) {
        Popup entity = popupRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }

        return new GetAdminPopupDetailResponse(entity);
    }

    public boolean insert(PostAdminPopupRequest dto) {
        dto.validate(cloudStorage);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Popup orderTop = popupRepository.findTop1ByOrderByOrderDesc();
        Long order = orderTop == null ? 0 : orderTop.getOrder() + 1;

        Popup entity = new Popup(dto, order);
        popupRepository.saveAndFlush(entity);
        logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));

        entity.applyTempFile(cloudStorage);
        popupRepository.flush();
        return true;
    }

    public boolean update(Long id, PostAdminPopupRequest dto) {
        dto.validate(cloudStorage);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Popup entity = popupRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }

        List<String> removeFiles = entity.getRemoveFile(dto);
        entity.update(dto);
        logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));

        entity.applyTempFile(cloudStorage);
        popupRepository.flush();

        cloudStorage.delete(removeFiles);
        return true;
    }

    public boolean delete(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Popup entity = popupRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }

        popupRepository.delete(entity);
        logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
        cloudStorage.deleteFolder(entity.getFileFolderPath());
        return true;
    }

    public boolean updateOrder(PutAdminPopupOrderRequest dto) {
        Popup dragEntity = popupRepository.findById(dto.getDragId()).orElse(null);
        Popup dropEntity = popupRepository.findById(dto.getDropId()).orElse(null);
        if (dragEntity == null || dropEntity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }

        Long dragOrder = dragEntity.getOrder();
        Long dropOrder = dropEntity.getOrder();

        List<Popup> list = popupRepository.search(dto, Math.min(dragOrder, dropOrder), Math.max(dragOrder, dropOrder));
        if (dragOrder > dropOrder) {
            //위에서 아래로 이동.
            for (int i = list.size() - 1; i >= 0; i--) {
                Popup x = list.get(i);
                if (i == 0) {
                    x.updateOrder(dropOrder);
                } else {
                    Popup pre = list.get(i - 1);
                    x.updateOrder(pre.getOrder());
                }
            }

        } else {
            //아래에서 위로 이동.
            for (int i = 0; i <= list.size() - 1; i++) {
                Popup x = list.get(i);

                if (i == list.size() - 1) {
                    list.get(list.size() - 1).updateOrder(dropOrder);
                } else {
                    Popup next = list.get(i + 1);
                    x.updateOrder(next.getOrder());
                }
            }
        }
        popupRepository.saveAll(list);
        popupRepository.flush();
        return true;
    }

    public boolean updateEnabled(Long id, boolean isEnabled) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Popup entity = popupRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        entity.updateEnabled(isEnabled);
        return true;
    }
}
