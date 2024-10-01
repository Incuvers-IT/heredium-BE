package art.heredium.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import art.heredium.domain.account.entity.NonUser;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.repository.NonUserRepository;
import art.heredium.domain.common.model.dto.response.Data;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.exhibition.entity.ExhibitionRound;
import art.heredium.domain.exhibition.model.dto.request.GetAdminExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.request.GetUserExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.request.PostAdminExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.response.*;
import art.heredium.domain.exhibition.repository.ExhibitionRepository;
import art.heredium.domain.holiday.repository.HolidayRepository;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.ncloud.bean.CloudStorage;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import art.heredium.niceId.service.NiceIdService;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ExhibitionService {

  private final ExhibitionRepository exhibitionRepository;
  private final TicketRepository ticketRepository;
  private final HolidayRepository holidayRepository;
  private final LogRepository logRepository;
  private final CloudStorage cloudStorage;
  private final NonUserRepository nonUserRepository;
  private final NiceIdService niceIdService;

  public Page<GetAdminExhibitionResponse> list(GetAdminExhibitionRequest dto, Pageable pageable) {
    return exhibitionRepository.search(dto, pageable);
  }

  public Slice<GetUserExhibitionResponse> list(GetUserExhibitionRequest dto, Pageable pageable) {
    return exhibitionRepository.search(dto, pageable).map(GetUserExhibitionResponse::new);
  }

  public GetAdminExhibitionDetailResponse detailByAdmin(Long id) {
    Exhibition entity = exhibitionRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    List<Ticket> tickets = getTicketsFromExhibitionRound(entity);
    Map<String, Long> ticketTotalNumber = getTicketTotalNumber(tickets);
    Map<String, Long> ticketUsedCount = getTicketUsedCount(tickets);

    return new GetAdminExhibitionDetailResponse(entity, ticketTotalNumber, ticketUsedCount);
  }

  public GetUserExhibitionDetailResponse detailByUser(Long id) {
    Exhibition entity = exhibitionRepository.findById(id).orElse(null);
    if (entity == null || !entity.getIsEnabled()) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    return new GetUserExhibitionDetailResponse(entity);
  }

  public boolean insert(PostAdminExhibitionRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Exhibition entity = new Exhibition(dto);
    exhibitionRepository.saveAndFlush(entity);
    logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    entity.applyTempFile(cloudStorage);
    exhibitionRepository.flush();
    return true;
  }

  public boolean update(Long id, PostAdminExhibitionRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Exhibition entity = exhibitionRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    List<String> removeFiles = entity.getRemoveFile(dto);
    entity.update(dto);
    logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    entity.applyTempFile(cloudStorage);
    exhibitionRepository.flush();

    cloudStorage.delete(removeFiles);
    return true;
  }

  public boolean delete(Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Exhibition entity = exhibitionRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    exhibitionRepository.delete(entity);
    logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
    cloudStorage.deleteFolder(entity.getFileFolderPath());
    return true;
  }

  public List<GetAdminExhibitionExposeTitleResponse> exposeTitle() {
    return exhibitionRepository.findAllByEndDateAfterNow().stream()
        .map(GetAdminExhibitionExposeTitleResponse::new)
        .collect(Collectors.toList());
  }

  public GetAdminExhibitionDetailRoundResponse detailRounds(Long id) {
    Exhibition entity = exhibitionRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    List<Ticket> tickets = getTicketsFromExhibitionRound(entity);
    Map<String, Long> ticketTotalNumber = getTicketTotalNumber(tickets);
    Map<String, Long> ticketUsedCount = getTicketUsedCount(tickets);

    return new GetAdminExhibitionDetailRoundResponse(entity, ticketTotalNumber, ticketUsedCount);
  }

  private Map<String, Long> getTicketUsedCount(List<Ticket> tickets) {
    return tickets.stream()
        .filter(ticket -> ticket.getState() == TicketStateType.USED)
        .collect(
            Collectors.groupingBy(Ticket::getProjectId, Collectors.summingLong(Ticket::getNumber)));
  }

  private Map<String, Long> getTicketTotalNumber(List<Ticket> tickets) {
    return tickets.stream()
        .collect(
            Collectors.groupingBy(Ticket::getProjectId, Collectors.summingLong(Ticket::getNumber)));
  }

  private List<Ticket> getTicketsFromExhibitionRound(Exhibition entity) {
    return ticketRepository.findAllByRound(null, null, TicketKindType.EXHIBITION, entity.getId());
  }

  public boolean updateNote(Long id, Data<String> data) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Exhibition entity = exhibitionRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    entity.updateNote(data.getData());
    logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));
    return true;
  }

  public GetUserExhibitionDetailRoundResponse detailRound(Long id) {
    Exhibition entity = exhibitionRepository.findById(id).orElse(null);
    if (entity == null || !entity.getIsEnabled()) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND, 1);
    }

    ProjectStateType state = entity.getState();
    if (!state.equals(ProjectStateType.BOOKING) && !state.equals(ProjectStateType.PROGRESS)) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND, 2);
    }

    LocalDate bookingStartDate = entity.getBookingStartDate().toLocalDate();
    LocalDate bookingEndDate = entity.getBookingEndDate().toLocalDate();

    List<Ticket> tickets =
        ticketRepository.findAllByRound(
            bookingStartDate.atTime(LocalTime.MIN),
            bookingEndDate.atTime(LocalTime.MAX),
            TicketKindType.EXHIBITION,
            entity.getId());
    Map<LocalDateTime, Long> ticketTotalNumber =
        tickets.stream()
            .collect(
                Collectors.groupingBy(
                    Ticket::getStartDate, Collectors.summingLong(Ticket::getNumber)));
    List<LocalDate> holidays =
        holidayRepository.findAllByDayBetween(bookingStartDate, bookingEndDate);

    return new GetUserExhibitionDetailRoundResponse(entity, ticketTotalNumber, holidays);
  }

  public Object detailRoundInfo(Long id, LocalDate date, String encodeData) {
    Long accountId = null;
    Long nonUserId = null;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !authentication.getPrincipal().equals("anonymousUser")) {
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      accountId = userPrincipal.getId();
    } else if (encodeData != null) {
      PostNiceIdEncryptResponse info = niceIdService.decrypt(encodeData);
      NonUser nonUser =
          nonUserRepository.findByPhoneAndHanaBankUuidIsNull(info.getMobileNo()).orElse(null);
      if (nonUser != null) {
        nonUserId = nonUser.getId();
      }
    }

    Exhibition entity = exhibitionRepository.findById(id).orElse(null);
    if (entity == null || !entity.getIsEnabled() || holidayRepository.existsByDay(date)) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    List<Ticket> tickets =
        ticketRepository.findAllByRound(
            date.atTime(LocalTime.MIN),
            date.atTime(LocalTime.MAX),
            TicketKindType.EXHIBITION,
            entity.getId());
    Map<String, Long> ticketTotalNumber = getTicketTotalNumber(tickets);
    Long ticketNumber =
        ticketRepository.sumTicketNumber(
            accountId, nonUserId, date.atStartOfDay(), TicketKindType.EXHIBITION, entity.getId());
    List<ExhibitionRound> rounds =
        entity.getRounds().stream()
            .filter(round -> round.getStartDate().toLocalDate().isEqual(date))
            .collect(Collectors.toList());

    return new GetUserExhibitionDetailRoundInfoResponse(ticketNumber, rounds, ticketTotalNumber);
  }
}
