package art.heredium.domain.notice.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.model.dto.response.NextRecord;
import art.heredium.domain.notice.entity.Notice;

@Getter
@Setter
public class GetUserNoticeDetailResponse {
  private Long id;
  private String title;
  private Boolean isNotice;
  private LocalDateTime postDate;
  private String contents;
  private List<Storage> files;
  private NextRecord prev;
  private NextRecord next;

  public GetUserNoticeDetailResponse(
      Notice entity, NextRecord previousRecord, NextRecord nextRecord) {
    this.id = entity.getId();
    this.title = entity.getTitle();
    this.isNotice = entity.getIsNotice();
    this.postDate = entity.getPostDate();
    this.contents = entity.getContents();
    this.files = entity.getFiles();
    this.prev = previousRecord;
    this.next = nextRecord;
  }
}
