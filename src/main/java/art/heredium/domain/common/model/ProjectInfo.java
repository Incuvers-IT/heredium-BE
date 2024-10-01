package art.heredium.domain.common.model;

import java.time.LocalDateTime;

public interface ProjectInfo {
  Storage getThumbnail();

  LocalDateTime getStartDate();

  LocalDateTime getEndDate();
}
