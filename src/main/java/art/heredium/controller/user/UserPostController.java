package art.heredium.controller.user;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.model.dto.response.MembershipResponse;
import art.heredium.service.MembershipService;
import art.heredium.domain.post.model.dto.response.GetEnabledPostsResponse;
import art.heredium.service.PostService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/posts")
public class UserPostController {

  private final MembershipService membershipService;
  private final PostService postService;

  @GetMapping("/enabled")
  public ResponseEntity<List<GetEnabledPostsResponse>> getEnabledPosts() {
    final List<GetEnabledPostsResponse> enabledPosts = postService.getEnabledPosts();
    return ResponseEntity.ok(enabledPosts);
  }

    @GetMapping(value = "/{post-id}/memberships")
    public ResponseEntity<List<MembershipResponse>> getAllMembershipsByPostIdAndEnabledTrue(
            @PathVariable(name = "post-id") long postId) {
        this.postService
                .findByIdAndIsEnabledTrue(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
        List<Membership> result = this.membershipService.findByPostIdAndIsEnabledTrue(postId);
        return ResponseEntity.ok(
                result.stream().map(MembershipResponse::new).collect(Collectors.toList()));
    }
}
