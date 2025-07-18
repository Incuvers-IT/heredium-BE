package art.heredium.domain.coupon.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RecurringCouponCreateRequest extends CouponCreateRequest {

  @JsonProperty("start_date")
  private LocalDateTime startDate;

  @JsonProperty("end_date")
  private LocalDateTime endDate;

  @JsonProperty("is_recurring")
  @NotNull
  private Boolean isRecurring;

  @JsonProperty("recipient_type")
  private List<Short> recipientType;

  @JsonProperty("send_day_of_month")
  private Integer sendDayOfMonth;

  @JsonProperty("period_in_days")
  private Integer periodInDays;

  @JsonProperty("marketing_consent_benefit")
  private Boolean marketingConsentBenefit;
}
