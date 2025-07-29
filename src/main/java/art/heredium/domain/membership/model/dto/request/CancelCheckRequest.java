package art.heredium.domain.membership.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CancelCheckRequest {

  @NotNull private Long accountId;
  @NotNull private Long relatedMileageId;
  @NotNull
  @Min(0) private Integer mileageAmount;
}
