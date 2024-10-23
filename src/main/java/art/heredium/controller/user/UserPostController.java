package art.heredium.controller.user;

import java.util.ArrayList;
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

  @GetMapping
  public ResponseEntity<PostDetailsResponse> getPostDetails() {
    final Post post =
        this.postService
            .findFirstByIsEnabledTrue()
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    List<Exhibition> futureExhibitions = new ArrayList<>();
    List<Exhibition> ongoingExhibitions = new ArrayList<>();
    List<Exhibition> completedExhibitions = new ArrayList<>();
    List<Program> futurePrograms = new ArrayList<>();
    List<Program> ongoingPrograms = new ArrayList<>();
    List<Program> completedPrograms = new ArrayList<>();
    if (post.getFutureExhibitionCount() != null && post.getFutureExhibitionCount() != 0) {
      futureExhibitions =
          this.exhibitionService.findFirstXByFutureAndIsEnabledTrue(
              post.getFutureExhibitionCount());
    }
    if (post.getOngoingExhibitionCount() != null && post.getOngoingExhibitionCount() != 0) {
      ongoingExhibitions =
          this.exhibitionService.findFirstXByOngoingAndIsEnabledTrue(
              post.getOngoingExhibitionCount());
    }
    if (post.getCompletedExhibitionCount() != null && post.getCompletedExhibitionCount() != 0) {
      completedExhibitions =
          this.exhibitionService.findFirstXByCompletedAndIsEnabledTrue(
              post.getCompletedExhibitionCount());
    }
    if (post.getFutureProgramCount() != null && post.getFutureProgramCount() != 0) {
      futurePrograms =
          this.programService.findFirstXByFutureAndIsEnabledTrue(post.getFutureProgramCount());
    }
    if (post.getOngoingProgramCount() != null && post.getOngoingProgramCount() != 0) {
      ongoingPrograms =
          this.programService.findFirstXByOngoingAndIsEnabledTrue(post.getOngoingProgramCount());
    }
    if (post.getCompletedProgramCount() != null && post.getCompletedProgramCount() != 0) {
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
