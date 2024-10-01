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
import art.heredium.domain.coffee.entity.Coffee;
import art.heredium.domain.coffee.entity.CoffeeRound;
import art.heredium.domain.coffee.model.dto.request.GetAdminCoffeeRequest;
import art.heredium.domain.coffee.model.dto.request.GetUserCoffeeRequest;
import art.heredium.domain.coffee.model.dto.request.PostAdminCoffeeRequest;
import art.heredium.domain.coffee.model.dto.response.*;
import art.heredium.domain.coffee.repository.CoffeeRepository;
import art.heredium.domain.common.model.dto.response.Data;
import art.heredium.domain.common.type.ProjectStateType;
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
public class CoffeeService {

  private final CoffeeRepository coffeeRepository;
  private final TicketRepository ticketRepository;
  private final HolidayRepository holidayRepository;
  private final LogRepository logRepository;
  private final CloudStorage cloudStorage;
  private final NonUserRepository nonUserRepository;
  private final NiceIdService niceIdService;

  public Page<GetAdminCoffeeResponse> list(GetAdminCoffeeRequest dto, Pageable pageable) {
    return coffeeRepository.search(dto, pageable);
  }

  public Slice<GetUserCoffeeResponse> list(GetUserCoffeeRequest dto, Pageable pageable) {
    return coffeeRepository.search(dto, pageable).map(GetUserCoffeeResponse::new);
  }

  public GetAdminCoffeeDetailResponse detailByAdmin(Long id) {
    Coffee entity = coffeeRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    List<Ticket> tickets = getTicketsFromCoffeeRound(entity);
    Map<String, Long> ticketTotalNumber = getTicketTotalNumber(tickets);
    Map<String, Long> ticketUsedCount = getTicketUsedCount(tickets);

    return new GetAdminCoffeeDetailResponse(entity, ticketTotalNumber, ticketUsedCount);
  }

  public GetUserCoffeeDetailResponse detailByUser(Long id) {
    Coffee entity = coffeeRepository.findById(id).orElse(null);
    if (entity == null || !entity.getIsEnabled()) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    return new GetUserCoffeeDetailResponse(entity);
  }

  public boolean insert(PostAdminCoffeeRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Coffee entity = new Coffee(dto);
    coffeeRepository.saveAndFlush(entity);
    logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    entity.applyTempFile(cloudStorage);
    coffeeRepository.flush();
    return true;
  }

  public boolean update(Long id, PostAdminCoffeeRequest dto) {
    dto.validate(cloudStorage);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Coffee entity = coffeeRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    List<String> removeFiles = entity.getRemoveFile(dto);
    entity.update(dto);
    logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    entity.applyTempFile(cloudStorage);
    coffeeRepository.flush();

    cloudStorage.delete(removeFiles);
    return true;
  }

  public boolean delete(Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Coffee entity = coffeeRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    coffeeRepository.delete(entity);
    logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
    cloudStorage.deleteFolder(entity.getFileFolderPath());
    return true;
  }

  public List<GetAdminCoffeeExposeTitleResponse> exposeTitle() {
    return coffeeRepository.findAllByEndDateAfterNow().stream()
        .map(GetAdminCoffeeExposeTitleResponse::new)
        .collect(Collectors.toList());
  }

  public GetAdminCoffeeDetailRoundResponse detailRounds(Long id) {
    Coffee entity = coffeeRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    List<Ticket> tickets = getTicketsFromCoffeeRound(entity);
    Map<String, Long> ticketTotalNumber = getTicketTotalNumber(tickets);
    Map<String, Long> ticketUsedCount = getTicketUsedCount(tickets);

    return new GetAdminCoffeeDetailRoundResponse(entity, ticketTotalNumber, ticketUsedCount);
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

  private List<Ticket> getTicketsFromCoffeeRound(Coffee entity) {
    LocalDateTime minStartDate =
        entity.getRounds().stream()
            .map(CoffeeRound::getStartDate)
            .min(LocalDateTime::compareTo)
            .orElse(null);
    LocalDateTime maxStartDate =
        entity.getRounds().stream()
            .map(CoffeeRound::getStartDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    return ticketRepository.findAllByRound(
        minStartDate, maxStartDate, TicketKindType.COFFEE, entity.getId());
  }

  public boolean updateNote(Long id, Data<String> data) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Coffee entity = coffeeRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    entity.updateNote(data.getData());
    logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));
    return true;
  }

  public GetUserCoffeeDetailRoundResponse detailRound(Long id) {
    Coffee entity = coffeeRepository.findById(id).orElse(null);
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
            TicketKindType.COFFEE,
            entity.getId());
    Map<LocalDateTime, Long> ticketTotalNumber =
        tickets.stream()
            .collect(
                Collectors.groupingBy(
                    Ticket::getStartDate, Collectors.summingLong(Ticket::getNumber)));
    List<LocalDate> holidays =
        holidayRepository.findAllByDayBetween(bookingStartDate, bookingEndDate);

    return new GetUserCoffeeDetailRoundResponse(entity, ticketTotalNumber, holidays);
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

    Coffee entity = coffeeRepository.findById(id).orElse(null);
    if (entity == null || !entity.getIsEnabled() || holidayRepository.existsByDay(date)) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    List<Ticket> tickets =
        ticketRepository.findAllByRound(
            date.atTime(LocalTime.MIN),
            date.atTime(LocalTime.MAX),
            TicketKindType.COFFEE,
            entity.getId());
    Map<String, Long> ticketTotalNumber = getTicketTotalNumber(tickets);
    Long ticketNumber =
        ticketRepository.sumTicketNumber(
            accountId, nonUserId, date.atStartOfDay(), TicketKindType.COFFEE, entity.getId());
    List<CoffeeRound> rounds =
        entity.getRounds().stream()
            .filter(round -> round.getStartDate().toLocalDate().isEqual(date))
            .collect(Collectors.toList());

    return new GetUserCoffeeDetailRoundInfoResponse(ticketNumber, rounds, ticketTotalNumber);
  }
}
