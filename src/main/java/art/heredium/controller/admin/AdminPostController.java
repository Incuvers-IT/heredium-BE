package art.heredium.controller.admin;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.model.dto.request.PostCreateRequest;
import art.heredium.domain.post.model.dto.request.PostUpdateRequest;
import art.heredium.domain.post.model.dto.response.AdminPostDetailsResponse;
import art.heredium.domain.post.model.dto.response.PostHistoryBaseResponse;
import art.heredium.domain.post.model.dto.response.PostHistoryResponse;
import art.heredium.service.MembershipService;
import art.heredium.service.PostHistoryService;
import art.heredium.service.PostService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
@ManagerPermission
public class AdminPostController {

  private final MembershipService membershipService;
  private final PostService postService;
  private final PostHistoryService postHistoryService;

  @PostMapping
  public ResponseEntity<Long> createPost(@Valid @RequestBody PostCreateRequest request) {

    if (!request.getIsEnabled() && request.getMemberships() != null) {
      throw new ApiException(
          ErrorCode.BAD_VALID, "Memberships cannot be created for disabled posts");
    }
    Optional<Post> existingPost = this.postService.findFirst();
    if (existingPost.isPresent()) throw new ApiException(ErrorCode.POST_ALREADY_EXISTED);

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

  @GetMapping("/history/search")
  public ResponseEntity<Page<PostHistoryBaseResponse>> search(
      @RequestParam(value = "modify_date_from", required = false)
          @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          LocalDateTime modifyDateFrom,
      @RequestParam(value = "modify_date_to", required = false)
          @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
          LocalDateTime modifyDateTo,
      @RequestParam(value = "modify_user", required = false) String modifyUser,
      Pageable pageable) {
    return ResponseEntity.ok(
        this.postHistoryService.listPostHistory(
            modifyDateFrom, modifyDateTo, modifyUser, pageable));
  }

  @GetMapping(value = "/history/{post_history_id}")
  public ResponseEntity<PostHistoryResponse> getPostHistory(
      @PathVariable(value = "post_history_id") Long postHistoryId) {
    return ResponseEntity.ok(this.postHistoryService.getPostHistory(postHistoryId));
  }
}
