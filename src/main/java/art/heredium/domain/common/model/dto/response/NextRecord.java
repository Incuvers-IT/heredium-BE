package art.heredium.domain.common.model.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NextRecord {
  private Long id;
  private String title;

  public NextRecord(Long id, String title) {
    this.id = id;
    this.title = title;
  }
}
