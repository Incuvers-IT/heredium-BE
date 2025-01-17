package art.heredium.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.post.model.dto.request.PostHistorySearchRequest;
import art.heredium.domain.post.model.dto.response.PostHistoryBaseResponse;

public interface PostHistoryRepositoryQueryDsl {
  Page<PostHistoryBaseResponse> search(PostHistorySearchRequest request, Pageable pageable);
}
