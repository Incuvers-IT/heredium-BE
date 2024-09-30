package art.heredium.service;

import art.heredium.domain.docent.entity.Docent;
import art.heredium.domain.docent.entity.DocentInfo;
import art.heredium.domain.docent.model.dto.request.GetAdminDocentRequest;
import art.heredium.domain.docent.model.dto.request.PostAdminDocentRequest;
import art.heredium.domain.docent.model.dto.response.*;
import art.heredium.domain.docent.repository.DocentInfoRepository;
import art.heredium.domain.docent.repository.DocentRepository;
import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.common.model.dto.response.NextRecord;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.ncloud.bean.CloudStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class DocentService {

    private final DocentRepository docentRepository;
    private final DocentInfoRepository docentInfoRepository;
    private final LogRepository logRepository;
    private final CloudStorage cloudStorage;

    public Page<GetAdminDocentResponse> list(GetAdminDocentRequest dto, Pageable pageable) {
        return docentRepository.search(dto, pageable).map(GetAdminDocentResponse::new);
    }

    public GetAdminDocentDetailResponse detailByAdmin(Long id) {
        Docent entity = docentRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }

        return new GetAdminDocentDetailResponse(entity);
    }

    public boolean insert(PostAdminDocentRequest dto) {
        dto.validate(cloudStorage);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Docent entity = new Docent(dto);
        docentRepository.saveAndFlush(entity);
        logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));

        entity.applyTempFile(cloudStorage);
        docentRepository.flush();
        return true;
    }

    public boolean update(Long id, PostAdminDocentRequest dto) {
        dto.validate(cloudStorage);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Docent entity = docentRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }

        List<String> removeFiles = entity.getRemoveFile(dto);
        entity.update(dto);
        logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));

        entity.applyTempFile(cloudStorage);
        docentRepository.flush();

        cloudStorage.delete(removeFiles);
        return true;
    }

    public boolean delete(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Docent entity = docentRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }

        docentRepository.delete(entity);
        logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
        cloudStorage.deleteFolder(entity.getFileFolderPath());
        return true;
    }

    public Page<GetAdminDocentDetailResponse.Info> infos(Long id, Pageable pageable) {
        return docentInfoRepository.findAllByDocent_IdOrderByOrderAsc(id, pageable)
                .map(GetAdminDocentDetailResponse.Info::new);
    }

    public List<GetUserDocentResponse> list() {
        return docentRepository.findPostingByUser().stream()
                .map(GetUserDocentResponse::new)
                .collect(Collectors.toList());
    }

    public GetUserDocentInfoResponse infos(Long id) {
        Docent entity = docentRepository.findPostingByUserAndId(id);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        List<GetUserDocentInfoResponse.Info> infos = docentInfoRepository.findPostingInfos(id)
                .stream()
                .map(GetUserDocentInfoResponse.Info::new)
                .collect(Collectors.toList());
        return new GetUserDocentInfoResponse(entity.getTitle(), infos);
    }

    public Object detailInfo(Long id, Long infoId) {
        DocentInfo entity = docentInfoRepository.findById(infoId).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        DocentInfo previous = docentInfoRepository.findPrev(entity.getDocent().getId(), entity.getId());
        DocentInfo next = docentInfoRepository.findNext(entity.getDocent().getId(), entity.getId());
        NextRecord previousRecord = previous != null ? new NextRecord(previous.getId(), previous.getTitle()) : null;
        NextRecord nextRecord = next != null ? new NextRecord(next.getId(), next.getTitle()) : null;
        return new GetUserDocentInfoDetailResponse(entity, previousRecord, nextRecord);
    }
}
