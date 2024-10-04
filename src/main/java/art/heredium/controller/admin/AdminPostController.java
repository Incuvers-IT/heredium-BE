package art.heredium.controller.admin;

import java.util.List;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.post.model.dto.request.PostUpdateRequest;
import art.heredium.service.PostService;
import art.heredium.domain.membership.model.dto.request.MultipleMembershipCreateRequest;
import art.heredium.domain.membership.model.dto.response.MultipleMembershipCreateResponse;
import art.heredium.domain.membership.model.dto.request.MembershipUpdateRequest;
import art.heredium.service.MembershipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
@ManagerPermission
public class AdminPostController {

  private final MembershipService membershipService;
  private final PostService postService;

  @PostMapping("/{post_id}/membership/add")
  public ResponseEntity<MultipleMembershipCreateResponse> createMemberships(
      @PathVariable(name = "post_id") Long postId,
      @RequestBody @Valid MultipleMembershipCreateRequest request) {
    final List<Long> membershipIds =
        membershipService.createMemberships(postId, request.getMemberships());
    return ResponseEntity.ok(new MultipleMembershipCreateResponse(membershipIds));
  }

    @PutMapping("/{membership-id}/update-is-enabled")
    public ResponseEntity updateIsEnabled(
            @PathVariable("membership-id") long membershipId,
            @RequestBody MembershipUpdateRequest request) {
        this.membershipService.updateIsEnabled(membershipId, request.getIsEnabled());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{post-id}/update-is-enabled")
    public ResponseEntity updateIsEnabled(
            @PathVariable("post-id") long postId, @RequestBody PostUpdateRequest request) {
        this.postService.updateIsEnabled(postId, request.getIsEnabled());
        return ResponseEntity.ok().build();
    }
}
