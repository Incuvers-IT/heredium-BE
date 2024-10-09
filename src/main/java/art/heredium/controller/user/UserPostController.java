package art.heredium.controller.user;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.post.model.dto.response.PostDetailsResponse;
import art.heredium.domain.post.model.dto.response.PostResponse;
import art.heredium.service.MembershipService;
import art.heredium.service.PostService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/posts")
public class UserPostController {

  private final MembershipService membershipService;
  private final PostService postService;

  @GetMapping("/enabled-list")
  public ResponseEntity<List<PostResponse>> getEnabledPosts() {
    final List<PostResponse> enabledPosts = postService.getEnabledPosts();
    return ResponseEntity.ok(enabledPosts);
  }

  @GetMapping(value = "/{post-id}")
  public ResponseEntity<PostDetailsResponse> getPostDetails(
      @PathVariable(name = "post-id") long postId) {
    return ResponseEntity.ok(
        this.postService
            .findByIdAndIsEnabledTrue(postId)
            .map(PostDetailsResponse::new)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND)));
  }
}
