package art.heredium.domain.company.model.dto.request;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class CompanyMembershipRegistrationRequest {
  @NotBlank private String title;

  @NotBlank
  @JsonProperty("email_or_phone")
  private String emailOrPhone;

  @NotNull
  @JsonProperty("start_date")
  private LocalDate startDate;

  @NotNull @Positive private Integer price;

  @NotNull
  @JsonProperty("payment_date")
  private LocalDate paymentDate;
}
