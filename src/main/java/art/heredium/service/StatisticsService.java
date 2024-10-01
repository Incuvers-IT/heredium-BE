package art.heredium.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.common.model.dto.response.ChartResponse;
import art.heredium.domain.statistics.model.dto.response.AdminStatisticsResponse;
import art.heredium.domain.statistics.model.dto.response.GetAdminStatisticsSummaryResponse;
import art.heredium.domain.statistics.repository.StatisticsRepository;
import art.heredium.domain.statistics.type.StatisticsDateType;
import art.heredium.domain.statistics.type.StatisticsType;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.domain.ticket.type.TicketKindType;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class StatisticsService {
  private final TicketRepository ticketRepository;
  private final AccountRepository accountRepository;
  private final StatisticsRepository statisticsRepository;

  public AdminStatisticsResponse statistics(LocalDateTime startDate, LocalDateTime endDate) {
    return new AdminStatisticsResponse(
        ticketRepository.sumVisitNumber(startDate, endDate),
        ticketRepository.sumPrice(startDate, endDate),
        accountRepository.countSignUp(startDate, endDate));
  }

  public GetAdminStatisticsSummaryResponse summary(
      LocalDateTime startDate, LocalDateTime endDate, TicketKindType kind, Long kindId) {
    return new GetAdminStatisticsSummaryResponse(
        ticketRepository.sumVisitNumber(startDate, endDate, kind, kindId),
        ticketRepository.sumBookingNumber(startDate, endDate, kind, kindId),
        ticketRepository.sumPrice(startDate, endDate, kind, kindId),
        statisticsRepository.ticketPriceInfo(startDate, endDate, kind, kindId));
  }

  public List<ChartResponse> ticketInfoChart(
      LocalDateTime startDate, LocalDateTime endDate, TicketKindType kind, Long kindId) {
    return statisticsRepository.ticketInfoChart(startDate, endDate, kind, kindId);
  }

  public List<ChartResponse> chart(
      LocalDateTime startDate,
      LocalDateTime endDate,
      TicketKindType kind,
      Long kindId,
      StatisticsType type,
      StatisticsDateType dateType) {
    return statisticsRepository.chart(startDate, endDate, kind, kindId, type, dateType);
  }
}
