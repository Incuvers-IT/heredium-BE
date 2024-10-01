package art.heredium.domain.coffee.model.dto.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.type.ProjectStateType;

@Getter
@Setter
public class GetUserCoffeeRequest {
  @NotNull private List<ProjectStateType> states;
}
