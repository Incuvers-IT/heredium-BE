package art.heredium.controller.admin;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.model.dto.request.PostCreateRequest;
import art.heredium.domain.post.model.dto.request.PostUpdateRequest;
import art.heredium.domain.post.model.dto.response.PostDetailsResponse;
import art.heredium.domain.program.entity.Program;
import art.heredium.service.ExhibitionService;
import art.heredium.service.MembershipService;
import art.heredium.service.PostService;
import art.heredium.service.ProgramService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
@ManagerPermission
public class AdminPostController {

  private final MembershipService membershipService;
  private final PostService postService;
  private final ExhibitionService exhibitionService;
  private final ProgramService programService;

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
  public ResponseEntity<PostDetailsResponse> getPostDetails() {
    final Post post =
        this.postService.findFirst().orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
    List<Exhibition> futureExhibitions = new ArrayList<>();
    List<Exhibition> ongoingExhibitions = new ArrayList<>();
    List<Exhibition> completedExhibitions = new ArrayList<>();
    List<Program> futurePrograms = new ArrayList<>();
    List<Program> ongoingPrograms = new ArrayList<>();
    List<Program> completedPrograms = new ArrayList<>();
    if (post.getFutureExhibitionCount() != 0) {
      futureExhibitions =
          this.exhibitionService.findFirstXByFutureAndIsEnabledTrue(
              post.getFutureExhibitionCount());
    }
    if (post.getOngoingExhibitionCount() != 0) {
      ongoingExhibitions =
          this.exhibitionService.findFirstXByOngoingAndIsEnabledTrue(
              post.getOngoingExhibitionCount());
    }
    if (post.getCompletedExhibitionCount() != 0) {
      completedExhibitions =
          this.exhibitionService.findFirstXByCompletedAndIsEnabledTrue(
              post.getCompletedExhibitionCount());
    }
    if (post.getFutureProgramCount() != 0) {
      futurePrograms =
          this.programService.findFirstXByFutureAndIsEnabledTrue(post.getFutureProgramCount());
    }
    if (post.getOngoingProgramCount() != 0) {
      ongoingPrograms =
          this.programService.findFirstXByOngoingAndIsEnabledTrue(post.getOngoingProgramCount());
    }
    if (post.getCompletedProgramCount() != 0) {
      completedPrograms =
          this.programService.findFirstXByCompletedAndIsEnabledTrue(
              post.getCompletedProgramCount());
    }
    return ResponseEntity.ok(
        new PostDetailsResponse(
            post,
            futureExhibitions,
            ongoingExhibitions,
            completedExhibitions,
            futurePrograms,
            ongoingPrograms,
            completedPrograms));
  }

  @PutMapping
  public ResponseEntity<Void> updatePost(@Valid @RequestBody PostUpdateRequest request) {
    postService.updatePost(request);
    return ResponseEntity.ok().build();
  }
}
