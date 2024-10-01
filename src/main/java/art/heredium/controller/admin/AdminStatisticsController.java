package art.heredium.controller.admin;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.core.annotation.CoffeePermission;
import art.heredium.domain.statistics.type.StatisticsDateType;
import art.heredium.domain.statistics.type.StatisticsType;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.service.StatisticsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

  private final StatisticsService statisticsService;

  @GetMapping
  @CoffeePermission
  public ResponseEntity statistics(
      @RequestParam("startDate") LocalDateTime startDate,
      @RequestParam("endDate") LocalDateTime endDate) {
    return ResponseEntity.ok(statisticsService.statistics(startDate, endDate));
  }

  @GetMapping("/summary")
  @CoffeePermission
  public ResponseEntity summary(
      @RequestParam("startDate") LocalDateTime startDate,
      @RequestParam("endDate") LocalDateTime endDate,
      @RequestParam("kind") TicketKindType kind,
      @RequestParam("kindId") Long kindId) {
    return ResponseEntity.ok(statisticsService.summary(startDate, endDate, kind, kindId));
  }

  @GetMapping("/chart/ticket-option")
  @CoffeePermission
  public ResponseEntity ticketInfoChart(
      @RequestParam("startDate") LocalDateTime startDate,
      @RequestParam("endDate") LocalDateTime endDate,
      @RequestParam(value = "kind", required = false) TicketKindType kind,
      @RequestParam(value = "kindId", required = false) Long kindId) {
    return ResponseEntity.ok(statisticsService.ticketInfoChart(startDate, endDate, kind, kindId));
  }

  @GetMapping("/chart")
  @CoffeePermission
  public ResponseEntity chart(
      @RequestParam("startDate") LocalDateTime startDate,
      @RequestParam("endDate") LocalDateTime endDate,
      @RequestParam(value = "kind", required = false) TicketKindType kind,
      @RequestParam(value = "kindId", required = false) Long kindId,
      @RequestParam("type") StatisticsType type,
      @RequestParam("dateType") StatisticsDateType dateType) {
    return ResponseEntity.ok(
        statisticsService.chart(startDate, endDate, kind, kindId, type, dateType));
  }
}
