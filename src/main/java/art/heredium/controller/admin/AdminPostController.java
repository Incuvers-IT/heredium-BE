package art.heredium.controller.admin;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.post.model.dto.request.PostUpdateRequest;
import art.heredium.service.PostService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
@ManagerPermission
public class AdminPostController {

  private final PostService postService;

  @PutMapping("/{post-id}/update-is-enabled")
  public ResponseEntity updateIsEnabled(
      @PathVariable("post-id") long postId, @RequestBody PostUpdateRequest request) {
    this.postService.updateIsEnabled(postId, request.getIsEnabled());
    return ResponseEntity.ok().build();
  }
}
