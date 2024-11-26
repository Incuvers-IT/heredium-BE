package art.heredium.domain.company.model.dto.request;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyMembershipRegistrationRequest {
  @NotBlank private String email;

  @NotBlank private String phone;

  @NotNull private LocalDate startDate;

  @NotNull @Positive private Long price;

  @NotNull private LocalDate paymentDate;
}
