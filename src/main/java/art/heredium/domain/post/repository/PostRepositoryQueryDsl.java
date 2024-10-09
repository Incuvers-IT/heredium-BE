package art.heredium.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.post.model.dto.request.GetAdminPostRequest;
import art.heredium.domain.post.model.dto.response.PostResponse;

public interface PostRepositoryQueryDsl {
  Page<PostResponse> search(GetAdminPostRequest dto, Pageable pageable);
}
