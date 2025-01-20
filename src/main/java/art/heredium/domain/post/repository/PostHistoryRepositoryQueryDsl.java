package art.heredium.domain.post.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.post.model.dto.response.PostHistoryBaseResponse;

public interface PostHistoryRepositoryQueryDsl {
  Page<PostHistoryBaseResponse> search(
      LocalDateTime modifyDateFrom,
      LocalDateTime modifyDateTo,
      String modifyUser,
      Pageable pageable);
}
