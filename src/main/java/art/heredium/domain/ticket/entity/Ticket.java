package art.heredium.domain.ticket.entity;

import java.io.Serializable;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.entity.NonUser;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.common.type.ProjectPriceType;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import art.heredium.domain.ticket.model.TicketInviteCreateInfo;
import art.heredium.domain.ticket.model.TicketUserInfo;
import art.heredium.domain.ticket.model.dto.request.PostAdminTicketGroupRequest;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;
import art.heredium.payment.inf.PaymentTicketResponse;
import art.heredium.payment.type.PaymentType;

@Entity
@Getter
@Table(name = "ticket")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"logs"})
// 티켓
public class Ticket implements Serializable {
  private static final long serialVersionUID = 7123638976927922703L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "non_user_id")
  private NonUser nonUser;

  @Comment("티켓 종류")
  @Convert(converter = TicketType.Converter.class)
  @Column(name = "type", nullable = false, updatable = false)
  private TicketType type;

  @Comment("구매 table 종류")
  @Convert(converter = TicketKindType.Converter.class)
  @Column(name = "kind", nullable = false, updatable = false)
  private TicketKindType kind;

  @Comment("구매 table id")
  @Column(name = "kind_id", nullable = false, updatable = false)
  private Long kindId;

  @Comment("회차 id")
  @Column(name = "round_id", updatable = false)
  private Long roundId;

  @Comment("제목")
  @Column(name = "title", nullable = false, length = 100, updatable = false)
  private String title;

  @Comment("시작일")
  @Column(name = "start_date", nullable = false, updatable = false)
  private LocalDateTime startDate;

  @Comment("종료일")
  @Column(name = "end_date", nullable = false, updatable = false)
  private LocalDateTime endDate;

  @Comment("구매수")
  @Column(name = "number", nullable = false, updatable = false)
  private Integer number;

  @Comment("최종 결제 가격")
  @Column(name = "price", nullable = false, updatable = false)
  private Long price;

  @Comment("가격(할인 전 가격)")
  @Column(name = "origin_price", nullable = false, updatable = false)
  private Long originPrice;

  @Comment("구매자 이메일")
  @Column(name = "email", nullable = false, length = 255, updatable = false)
  private String email;

  @Comment("구매자 이름")
  @Column(name = "name", nullable = false, length = 30, updatable = false)
  private String name;

  @Comment("구매자 핸드폰 번호")
  @Column(name = "phone", nullable = false, length = 15, updatable = false)
  private String phone;

  @Comment("티켓 uuid")
  @Column(name = "uuid", nullable = false, length = 36, unique = true, updatable = false)
  private String uuid;

  @Comment("티켓 확인용 비밀번호")
  @Column(name = "password", nullable = false, length = 4, updatable = false)
  private String password;

  @Comment("pg id")
  @Column(name = "pg_id", length = 100, updatable = false)
  private String pgId;

  @Comment("pg 결제방법")
  @Column(name = "pay_method", nullable = false, length = 30, updatable = false)
  private String payMethod;

  @Comment("티켓 사용일(입장일)")
  @Column(name = "used_date")
  private LocalDateTime usedDate;

  @Comment("티켓 상태")
  @Convert(converter = TicketStateType.Converter.class)
  @Column(name = "state", nullable = false)
  private TicketStateType state;

  @Comment("pg 종류")
  @Convert(converter = PaymentType.Converter.class)
  @Column(name = "payment")
  private PaymentType payment;

  @Comment("티켓 입장 예약 알림톡 요청 id")
  @Type(type = "json")
  @Column(name = "sms_request_id", columnDefinition = "json")
  private List<String> smsRequestId;

  @Comment("생성일")
  @CreatedDate
  @Column(name = "created_date", nullable = false, updatable = false)
  private LocalDateTime createdDate;

  @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
  private List<TicketPrice> prices = new ArrayList<>();

  @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdDate DESC")
  private List<TicketLog> logs = new ArrayList<>();

  @Comment("coupon-uuid")
  @Column(name = "coupon_uuid", length = 100)
  private String couponUuid;

  @Comment("already-refund-coupon")
  @Column(name = "is_coupon_already_refund")
  private Boolean isCouponAlreadyRefund;

  public Ticket(
      List<TicketPrice> prices,
      TicketUserInfo ticketUserInfo,
      TicketKindType kind,
      Long kindId,
      Long roundId,
      Account account,
      NonUser nonuser,
      String uuid,
      String title,
      LocalDateTime startDate,
      LocalDateTime endDate) {
    this.account = account;
    this.nonUser = nonuser;
    this.type = TicketType.NORMAL;
    this.kind = kind;
    this.kindId = kindId;
    this.roundId = roundId;
    this.title = title;
    this.startDate = startDate;
    this.endDate = endDate;
    this.number = prices.stream().mapToInt(TicketPrice::getNumber).sum();
    this.price = prices.stream().mapToLong(price -> price.getNumber() * price.getPrice()).sum();
    this.originPrice =
        prices.stream().mapToLong(price -> price.getNumber() * price.getOriginPrice()).sum();
    this.email = ticketUserInfo.getEmail();
    this.name = ticketUserInfo.getName();
    this.phone = ticketUserInfo.getPhone();
    this.password = ticketUserInfo.getPassword();
    this.payMethod = "";
    this.pgId = "";
    this.uuid = uuid;
    this.state = TicketStateType.PAYMENT;
    prices.forEach(price -> price.setTicket(this));
    this.prices = prices;
    this.logs.add(new TicketLog(this, null, this.getName(), null, this.getState()));
    this.isCouponAlreadyRefund = false;
  }

  public Ticket(
      PostAdminTicketGroupRequest dto, TicketCreateInfo info, Account account, Admin admin) {
    this.account = account;
    this.type = TicketType.GROUP;
    this.kind = info.getKind();
    this.kindId = info.getKindId();
    this.roundId = info.getRoundId();
    this.title = info.getTitle();
    this.startDate = info.getRoundStartDate();
    this.endDate = info.getRoundEndDate();
    this.number = dto.getNumber();
    this.price = dto.getPrice();
    this.originPrice = 0L;
    this.password = "";
    this.email = account.getEmail();
    this.name = account.getAccountInfo().getName();
    this.phone = account.getAccountInfo().getPhone();
    this.payMethod = "무통장";
    this.uuid = Constants.getUUID();
    this.state = TicketStateType.PAYMENT;
    this.prices.add(
        new TicketPrice(
            this,
            ProjectPriceType.GROUP.getDesc(),
            this.getNumber(),
            this.getPrice() / this.getNumber(),
            this.getPrice() / this.getNumber(),
            null));
    this.logs.add(
        new TicketLog(this, admin, admin.getAdminInfo().getName(), null, this.getState()));
    this.isCouponAlreadyRefund = false;
  }

  public Ticket(TicketInviteCreateInfo info, Account account, Admin admin) {
    this.account = account;
    this.type = TicketType.INVITE;
    this.kind = info.getKind();
    this.kindId = info.getId();
    this.roundId = null;
    this.title = info.getTitle();
    this.startDate = info.getStartDate();
    this.endDate = info.getEndDate();
    this.number = info.getNumber();
    this.price = 0L;
    this.originPrice = 0L;
    this.password = "";
    this.email = account.getEmail();
    this.name = account.getAccountInfo().getName();
    this.phone = account.getAccountInfo().getPhone();
    this.payMethod = "";
    this.uuid = Constants.getUUID();
    this.state = TicketStateType.ISSUANCE;
    this.prices.add(
        new TicketPrice(this, ProjectPriceType.INVITE.getDesc(), this.getNumber(), 0L, 0L, null));
    this.logs.add(
        new TicketLog(this, admin, admin.getAdminInfo().getName(), null, this.getState()));
    this.isCouponAlreadyRefund = false;
  }

  public Log createInsertLog(Admin admin) {
    return new Log(admin, this.title, this.toString(), LogType.TICKET, LogAction.INSERT);
  }

  public Log createUpdateLog(Admin admin) {
    return new Log(admin, this.title, this.toString(), LogType.TICKET, LogAction.UPDATE);
  }

  public void updateState(UserPrincipal userPrincipal, TicketStateType state) {
    this.logs.add(
        new TicketLog(
            this, userPrincipal.getAdmin(), userPrincipal.getName(), this.getState(), state));
    this.state = state;
  }

  public void updateState(TicketStateType state) {
    this.logs.add(new TicketLog(this, null, this.getName(), this.getState(), state));
    this.state = state;
  }

  public void updateUsedDate() {
    this.usedDate = Constants.getNow();
  }

  public void updateSmsRequestId(List<String> smsRequestId) {
    this.smsRequestId = smsRequestId;
  }

  public void updateCouponUuid(String couponUuid) {
    this.couponUuid = couponUuid;
  }

  public void setCouponAlreadyRefund(boolean isCouponAlreadyRefund) {
    this.isCouponAlreadyRefund = isCouponAlreadyRefund;
  }

  public void setPrice(long price) {
    this.price = price;
  }

  public boolean isRefund() {
    return (this.getState().equals(TicketStateType.ADMIN_REFUND)
        || this.getState().equals(TicketStateType.USER_REFUND));
  }

  public boolean isCanUse() {
    return this.getState().equals(TicketStateType.PAYMENT)
        || this.getState().equals(TicketStateType.ISSUANCE);
  }

  public boolean isNormalPayment() {
    return this.getState().equals(TicketStateType.PAYMENT)
        && this.getType().equals(TicketType.NORMAL);
  }

  public boolean isBefore24Hour() {
    LocalDateTime now = Constants.getNow();
    return now.isBefore(this.getStartDate().minusHours(24));
  }

  public Map<String, String> getMailParam(HerediumProperties herediumProperties) {
    Map<String, String> param = new HashMap<>();
    param.put("name", this.getName());
    param.put("title", this.getTitle());
    param.put("uuid", this.getUuid());
    param.put(
        "startDate",
        this.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E) HH:mm")));
    param.put("month", this.getStartDate().format(DateTimeFormatter.ofPattern("MM")));
    param.put("day", this.getStartDate().format(DateTimeFormatter.ofPattern("dd")));
    param.put(
        "info",
        this.getPrices().stream()
            .map(
                ticketPrice ->
                    String.format("%s %d매", ticketPrice.getType(), ticketPrice.getNumber()))
            .collect(Collectors.joining(", ")));
    param.put("price", NumberFormat.getInstance().format(this.getPrice()));
    param.put("CSTel", herediumProperties.getTel());
    param.put("CSEmail", herediumProperties.getEmail());
    return param;
  }

  public void setPrices(List<TicketPrice> prices) {
    this.prices = prices;
  }

  public void initPay(PaymentTicketResponse res, PaymentType paymentType) {
    this.payMethod = res.getPayMethod();
    this.pgId = res.getPaymentKey();
    this.price = res.getPaymentAmount();
    this.payment = paymentType;
  }

  public String getProjectId() {
    String projectId = this.getKind().getDesc() + "-" + this.getKindId();
    if (this.getRoundId() != null) {
      projectId += ("-" + this.getRoundId());
    }
    return projectId;
  }
}
