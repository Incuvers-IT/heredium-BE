package art.heredium.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.domain.slide.entity.Slide;
import art.heredium.domain.slide.model.dto.request.GetAdminSlideRequest;
import art.heredium.domain.slide.model.dto.request.PostAdminSlideRequest;
import art.heredium.domain.slide.model.dto.request.PutAdminSlideOrderRequest;
import art.heredium.domain.slide.model.dto.response.GetAdminSlideDetailResponse;
import art.heredium.domain.slide.model.dto.response.GetAdminSlideResponse;
import art.heredium.domain.slide.repository.SlideRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SlideService {

  private final SlideRepository slideRepository;
  private final LogRepository logRepository;
  private final CloudStorage cloudStorage;

  public Page<GetAdminSlideResponse> list(GetAdminSlideRequest dto, Pageable pageable) {
    return slideRepository.search(dto, pageable).map(GetAdminSlideResponse::new);
  }

  public GetAdminSlideDetailResponse detailByAdmin(Long id) {
    Slide entity = slideRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    return new GetAdminSlideDetailResponse(entity);
  }

  public boolean insert(PostAdminSlideRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Slide orderTop = slideRepository.findTop1ByOrderByOrderDesc();
    Long order = orderTop == null ? 0 : orderTop.getOrder() + 1;

    Slide entity = new Slide(dto, order);
    slideRepository.saveAndFlush(entity);
    logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));

    entity.applyTempFile(cloudStorage);
    slideRepository.flush();
    return true;
  }

  public boolean update(Long id, PostAdminSlideRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Slide entity = slideRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    List<String> removeFiles = entity.getRemoveFile(dto);
    entity.update(dto);
    logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));

    entity.applyTempFile(cloudStorage);
    slideRepository.flush();

    cloudStorage.delete(removeFiles);
    return true;
  }

  public boolean delete(Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Slide entity = slideRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    slideRepository.delete(entity);
    logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
    cloudStorage.deleteFolder(entity.getFileFolderPath());
    return true;
  }

  public boolean updateOrder(PutAdminSlideOrderRequest dto) {
    Slide dragEntity = slideRepository.findById(dto.getDragId()).orElse(null);
    Slide dropEntity = slideRepository.findById(dto.getDropId()).orElse(null);
    if (dragEntity == null || dropEntity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    Long dragOrder = dragEntity.getOrder();
    Long dropOrder = dropEntity.getOrder();

    List<Slide> list =
        slideRepository.search(dto, Math.min(dragOrder, dropOrder), Math.max(dragOrder, dropOrder));
    if (dragOrder > dropOrder) {
      // 위에서 아래로 이동.
      for (int i = list.size() - 1; i >= 0; i--) {
        Slide x = list.get(i);
        if (i == 0) {
          x.updateOrder(dropOrder);
        } else {
          Slide pre = list.get(i - 1);
          x.updateOrder(pre.getOrder());
        }
      }

    } else {
      // 아래에서 위로 이동.
      for (int i = 0; i <= list.size() - 1; i++) {
        Slide x = list.get(i);

        if (i == list.size() - 1) {
          list.get(list.size() - 1).updateOrder(dropOrder);
        } else {
          Slide next = list.get(i + 1);
          x.updateOrder(next.getOrder());
        }
      }
    }
    slideRepository.saveAll(list);
    slideRepository.flush();
    return true;
  }

  public boolean updateEnabled(Long id, boolean isEnabled) {
    Slide entity = slideRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    entity.updateEnabled(isEnabled);
    return true;
  }
}
