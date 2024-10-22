package art.heredium.controller.user;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.model.dto.response.PostDetailsResponse;
import art.heredium.domain.post.model.dto.response.PostResponse;
import art.heredium.domain.program.entity.Program;
import art.heredium.service.ExhibitionService;
import art.heredium.service.MembershipService;
import art.heredium.service.PostService;
import art.heredium.service.ProgramService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/posts")
public class UserPostController {

  private final MembershipService membershipService;
  private final PostService postService;
  private final ExhibitionService exhibitionService;
  private final ProgramService programService;

  @GetMapping("/enabled-list")
  public ResponseEntity<List<PostResponse>> getEnabledPosts() {
    final List<PostResponse> enabledPosts = postService.getEnabledPosts();
    return ResponseEntity.ok(enabledPosts);
  }

  @GetMapping
  public ResponseEntity<PostDetailsResponse> getPostDetails() {
    final Post post =
        this.postService
            .findFirstByIsEnabledTrue()
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    List<Exhibition> futureExhibitions = null;
    List<Exhibition> ongoingExhibitions = null;
    List<Exhibition> completedExhibitions = null;
    List<Program> futurePrograms = null;
    List<Program> ongoingPrograms = null;
    List<Program> completedPrograms = null;
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
}
