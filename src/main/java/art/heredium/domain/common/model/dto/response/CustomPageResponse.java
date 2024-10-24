package art.heredium.domain.common.model.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.domain.Page;

@Getter
@Setter
public class CustomPageResponse<T> {
  private List<T> content;
  private int totalPages;
  private long totalElements;
  private boolean last;
  private boolean first;
  private int currentPage;

  public CustomPageResponse(Page<T> page) {
    this.content = page.getContent();
    this.totalPages = page.getTotalPages();
    this.totalElements = page.getTotalElements();
    this.last = page.isLast();
    this.first = page.isFirst();
    this.currentPage = page.getNumber();
  }
}
