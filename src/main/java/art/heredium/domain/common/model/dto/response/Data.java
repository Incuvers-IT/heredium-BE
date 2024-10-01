package art.heredium.domain.common.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class Data<T> {
  private T data;
}
