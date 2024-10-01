package art.heredium.domain.event.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.model.dto.response.NextRecord;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.event.entity.Event;

@Getter
@Setter
public class GetUserEventDetailResponse {
  private Long id;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String contents;
  private List<Storage> files;
  private Storage thumbnail;
  private DateState state;
  private NextRecord prev;
  private NextRecord next;

  public GetUserEventDetailResponse(
      Event entity, NextRecord previousRecord, NextRecord nextRecord) {
    this.id = entity.getId();
    this.title = entity.getTitle();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.contents = entity.getContents();
    this.files = entity.getFiles();
    this.thumbnail = entity.getThumbnail();
    this.state = entity.getState();
    this.prev = previousRecord;
    this.next = nextRecord;
  }
}
