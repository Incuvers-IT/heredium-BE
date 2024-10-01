package art.heredium.domain.notice.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.notice.entity.Notice;

@Getter
@Setter
public class GetAdminNoticeResponse {
  private Long id;
  private String title;
  private Boolean isNotice;
  private LocalDateTime postDate;
  private String createdName;
  private LocalDateTime createdDate;
  private String lastModifiedName;
  private LocalDateTime lastModifiedDate;
  private Boolean isEnabled;

  public GetAdminNoticeResponse(Notice entity) {
    this.id = entity.getId();
    this.title = entity.getTitle();
    this.isNotice = entity.getIsNotice();
    this.postDate = entity.getPostDate();
    this.createdName = entity.getCreatedName();
    this.createdDate = entity.getCreatedDate();
    this.lastModifiedName = entity.getLastModifiedName();
    this.lastModifiedDate = entity.getLastModifiedDate();
    this.isEnabled = entity.getIsEnabled();
  }
}
