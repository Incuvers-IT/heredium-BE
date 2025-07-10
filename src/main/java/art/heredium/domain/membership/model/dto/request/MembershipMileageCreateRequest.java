package art.heredium.domain.membership.model.dto.request;

import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class MembershipMileageCreateRequest {

  @JsonProperty("accountId")
  private Long accountId;

  @JsonProperty("category")
  private Integer category;

  @JsonProperty("categoryId")
  private Long categoryId;

  @JsonProperty("type")
  private Long type;

  @JsonProperty("paymentMethod")
  private Integer paymentMethod;

  @JsonProperty("serialNumber")
  private String serialNumber;

  @JsonProperty("paymentAmount")
  private Integer paymentAmount;

  @JsonProperty("mileageAmount")
  private Integer mileageAmount;
}
