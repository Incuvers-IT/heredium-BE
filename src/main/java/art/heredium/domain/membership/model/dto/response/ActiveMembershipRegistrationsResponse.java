package art.heredium.domain.membership.model.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.PaymentStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveMembershipRegistrationsResponse {

  @JsonProperty("membership_name")
  private String membership;

  @JsonProperty("account_id")
  private Long accountId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("payment_status")
  private PaymentStatus paymentStatus;

  @JsonProperty("payment_date")
  private LocalDate paymentDate;

  @JsonProperty("number_of_memberships")
  private Long numberOfMemberships;

  @JsonProperty("number_of_exhibitions_used")
  private Long numberOfExhibitionsUsed;

  @JsonProperty("number_of_programs_used")
  private Long numberOfProgramsUsed;

  @JsonProperty("number_of_coffee_used")
  private Long numberOfCoffeeUsed;

  @JsonProperty("is_agree_to_receive_marketing")
  private Boolean isAgreeToReceiveMarketing;
}
