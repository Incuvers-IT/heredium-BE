package art.heredium.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.common.model.dto.response.NextRecord;
import art.heredium.domain.event.entity.Event;
import art.heredium.domain.event.model.dto.request.GetAdminEventRequest;
import art.heredium.domain.event.model.dto.request.PostAdminEventRequest;
import art.heredium.domain.event.model.dto.response.GetAdminEventDetailResponse;
import art.heredium.domain.event.model.dto.response.GetAdminEventResponse;
import art.heredium.domain.event.model.dto.response.GetUserEventDetailResponse;
import art.heredium.domain.event.model.dto.response.GetUserEventResponse;
import art.heredium.domain.event.repository.EventRepository;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EventService {

  private final EventRepository eventRepository;
  private final LogRepository logRepository;
  private final CloudStorage cloudStorage;

  public Page<GetAdminEventResponse> list(GetAdminEventRequest dto, Pageable pageable) {
    return eventRepository.home(dto, pageable).map(GetAdminEventResponse::new);
  }

  public GetAdminEventDetailResponse detailByAdmin(Long id) {
    Event entity = eventRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    return new GetAdminEventDetailResponse(entity);
  }

  public boolean insert(PostAdminEventRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Event entity = new Event(dto);
    eventRepository.saveAndFlush(entity);
    logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    entity.applyTempFile(cloudStorage);
    eventRepository.flush();
    return true;
  }

  public boolean update(Long id, PostAdminEventRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Event entity = eventRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    List<String> removeFiles = entity.getRemoveFile(dto);
    entity.update(dto);
    logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    entity.applyTempFile(cloudStorage);
    eventRepository.flush();

    cloudStorage.delete(removeFiles);
    return true;
  }

  public boolean delete(Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Event entity = eventRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    eventRepository.delete(entity);
    logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
    cloudStorage.deleteFolder(entity.getFileFolderPath());
    return true;
  }

  public Slice<GetUserEventResponse> listByUser(Pageable pageable) {
    return eventRepository.home(pageable).map(GetUserEventResponse::new);
  }

  public GetUserEventDetailResponse detailByUser(Long id) {
    Event entity = eventRepository.findById(id).orElse(null);
    if (entity == null || !entity.getIsEnabled()) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    Event previous = eventRepository.findPrev(entity.getId());
    Event next = eventRepository.findNext(entity.getId());
    NextRecord previousRecord =
        previous != null ? new NextRecord(previous.getId(), previous.getTitle()) : null;
    NextRecord nextRecord = next != null ? new NextRecord(next.getId(), next.getTitle()) : null;
    return new GetUserEventDetailResponse(entity, previousRecord, nextRecord);
  }
}
