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
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.common.model.dto.response.NextRecord;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.domain.notice.entity.Notice;
import art.heredium.domain.notice.model.dto.request.GetAdminNoticeRequest;
import art.heredium.domain.notice.model.dto.request.PostAdminNoticeRequest;
import art.heredium.domain.notice.model.dto.response.GetAdminNoticeDetailResponse;
import art.heredium.domain.notice.model.dto.response.GetAdminNoticeResponse;
import art.heredium.domain.notice.model.dto.response.GetUserNoticeDetailResponse;
import art.heredium.domain.notice.model.dto.response.GetUserNoticeResponse;
import art.heredium.domain.notice.repository.NoticeRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class NoticeService {

  private final NoticeRepository noticeRepository;
  private final LogRepository logRepository;
  private final CloudStorage cloudStorage;

  public Page<GetAdminNoticeResponse> list(GetAdminNoticeRequest dto, Pageable pageable) {
    return noticeRepository.search(dto, pageable).map(GetAdminNoticeResponse::new);
  }

  public GetAdminNoticeDetailResponse detailByAdmin(Long id) {
    Notice entity = noticeRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    return new GetAdminNoticeDetailResponse(entity);
  }

  public boolean insert(PostAdminNoticeRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Notice entity = new Notice(dto);
    noticeRepository.saveAndFlush(entity);
    logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    entity.applyTempFile(cloudStorage);
    noticeRepository.flush();
    return true;
  }

  public boolean update(Long id, PostAdminNoticeRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Notice entity = noticeRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    List<String> removeFiles = entity.getRemoveFile(dto);
    entity.update(dto);
    logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    entity.applyTempFile(cloudStorage);
    noticeRepository.flush();

    cloudStorage.delete(removeFiles);
    return true;
  }

  public boolean delete(Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Notice entity = noticeRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    noticeRepository.delete(entity);
    logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
    cloudStorage.deleteFolder(entity.getFileFolderPath());
    return true;
  }

  public Page<GetUserNoticeResponse> listByUser(Pageable pageable) {
    return noticeRepository.search(pageable).map(GetUserNoticeResponse::new);
  }

  public GetUserNoticeDetailResponse detailByUser(Long id) {
    Notice entity = noticeRepository.findById(id).orElse(null);
    if (entity == null
        || !entity.getIsEnabled()
        || !entity.getPostDate().isBefore(Constants.getNow())) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    Notice previous = noticeRepository.findPrev(entity.getId());
    Notice next = noticeRepository.findNext(entity.getId());
    NextRecord previousRecord =
        previous != null ? new NextRecord(previous.getId(), previous.getTitle()) : null;
    NextRecord nextRecord = next != null ? new NextRecord(next.getId(), next.getTitle()) : null;
    return new GetUserNoticeDetailResponse(entity, previousRecord, nextRecord);
  }
}
