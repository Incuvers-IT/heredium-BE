package art.heredium.domain.program.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.domain.common.type.ProjectPriceType;
import art.heredium.domain.program.model.dto.request.PostAdminProgramRequest;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import art.heredium.domain.ticket.model.TicketInfo;
import art.heredium.domain.ticket.model.TicketOrderInfo;
import art.heredium.domain.ticket.model.dto.request.PostAdminTicketGroupRequest;
import art.heredium.domain.ticket.type.TicketKindType;

@Entity
@Getter
@Table(name = "program_round")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"program"})
// 프로그램 회차
public class ProgramRound implements TicketInfo, Serializable {
  private static final long serialVersionUID = -2936560845069267375L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "program_id", nullable = false)
  private Program program;

  @Comment("시작일")
  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Comment("종료일")
  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @Comment("주문 제한수")
  @Column(name = "limit_number", nullable = false)
  private Integer limitNumber;

  public ProgramRound(PostAdminProgramRequest.Round dto, Program program) {
    this.program = program;
    this.startDate = dto.getStartDate();
    this.endDate = dto.getEndDate();
    this.limitNumber = dto.getLimitNumber();
  }

  public void update(PostAdminProgramRequest.Round dto) {
    this.startDate = dto.getStartDate();
    this.endDate = dto.getEndDate();
    this.limitNumber = dto.getLimitNumber();
  }

  public boolean isClose() {
    LocalDateTime now = Constants.getNow();
    LocalDateTime ticketLimitDate =
        this.getStartDate().isBefore(this.getEndDate().minus(30, ChronoUnit.MINUTES))
            ? this.getEndDate().minus(30, ChronoUnit.MINUTES)
            : this.getStartDate();
    return ticketLimitDate.isBefore(now);
  }

  @Override
  public String getTicketId() {
    return TicketKindType.PROGRAM.getDesc() + "-" + this.getProgram().getId() + "-" + this.getId();
  }

  @Override
  public boolean isEnabledTicket() {
    return program.getIsEnabled();
  }

  @Override
  public TicketCreateInfo getTicketCreateInfo(TicketOrderInfo dto) {
    Program entity = this.getProgram();

    List<TicketCreateInfo.Price> prices =
        entity.getPrices().stream()
            .filter(
                price ->
                    dto.getPrices().stream()
                        .anyMatch(dp -> dp.getId().longValue() == price.getId().longValue()))
            .map(
                price -> {
                  TicketOrderInfo.Price dtoPrice =
                      dto.getPrices().stream()
                          .filter(dp -> price.getId().longValue() == dp.getId())
                          .findFirst()
                          .orElse(null);
                  if (dtoPrice == null) {
                    throw new ApiException(ErrorCode.DATA_NOT_FOUND);
                  }
                  Long discountPrice = null;
                  String note = null;
                  ProgramPriceDiscount discount =
                      price.getDiscounts().stream()
                          .filter(priceDiscount -> priceDiscount.getType() == dto.getDiscountType())
                          .findAny()
                          .orElse(null);
                  if (discount != null && discount.getEnabled()) {
                    discountPrice = discount.getPrice();
                    note = discount.getNote();
                  }
                  return new TicketCreateInfo.Price(
                      price.getType(), dtoPrice.getNumber(), price.getPrice(), discountPrice, note);
                })
            .collect(Collectors.toList());

    return new TicketCreateInfo(
        TicketKindType.PROGRAM,
        entity.getId(),
        this.getId(),
        entity.getTitle(),
        entity.getStartDate(),
        entity.getEndDate(),
        this.getStartDate(),
        this.getEndDate(),
        this.getLimitNumber(),
        entity.getBookingEndDate(),
        this.isClose(),
        prices,
        dto.getDiscountType());
  }

  @Override
  public TicketCreateInfo getTicketCreateInfo(PostAdminTicketGroupRequest dto) {
    Program entity = this.getProgram();
    List<TicketCreateInfo.Price> prices = new ArrayList<>();
    TicketCreateInfo.Price price =
        new TicketCreateInfo.Price(
            ProjectPriceType.GROUP.getDesc(), dto.getNumber(), dto.getPrice(), null, null);
    prices.add(price);

    return new TicketCreateInfo(
        TicketKindType.PROGRAM,
        entity.getId(),
        this.getId(),
        entity.getTitle(),
        entity.getStartDate(),
        entity.getEndDate(),
        this.getStartDate(),
        this.getEndDate(),
        this.getLimitNumber(),
        entity.getBookingEndDate(),
        this.isClose(),
        prices,
        null);
  }
}
