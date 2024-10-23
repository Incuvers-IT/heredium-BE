package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.post.model.dto.request.PostCreateRequest;
import art.heredium.domain.post.model.dto.request.PostUpdateRequest;
import art.heredium.domain.post.model.dto.response.AdminPostDetailsResponse;
import art.heredium.service.MembershipService;
import art.heredium.service.PostService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
@ManagerPermission
public class AdminPostController {

  private final MembershipService membershipService;
  private final PostService postService;

  @PostMapping
  public ResponseEntity<Long> createPost(@Valid @RequestBody PostCreateRequest request) {

    if (!request.getIsEnabled() && request.getMemberships() != null) {
      throw new ApiException(
          ErrorCode.BAD_VALID, "Memberships cannot be created for disabled posts");
    }

    Long postId = postService.createPost(request);

    return ResponseEntity.ok(postId);
  }

  @GetMapping(value = "/details")
  public ResponseEntity<AdminPostDetailsResponse> getPostDetails() {
    return ResponseEntity.ok(
        this.postService
            .findFirst()
            .map(AdminPostDetailsResponse::new)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND)));
  }

  @PutMapping
  public ResponseEntity<Void> updatePost(@Valid @RequestBody PostUpdateRequest request) {
    postService.updatePost(request);
    return ResponseEntity.ok().build();
  }
}
