package art.heredium.domain.common.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import com.querydsl.core.annotations.QueryProjection;

@Getter
@Setter
public class ChartResponse {
  private String label;
  private Double value;

  @QueryProjection
  public ChartResponse(String label, Double value) {
    this.label = label;
    this.value = value;
  }
}
