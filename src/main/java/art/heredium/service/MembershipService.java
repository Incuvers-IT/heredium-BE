package art.heredium.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.repository.MembershipRepository;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.repository.PostRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

  private final MembershipRepository membershipRepository;
  private final PostRepository postRepository;

  public List<Membership> findByPostIdAndIsEnabledTrue(long postId) {
    return this.membershipRepository.findByPostIdAndIsEnabledTrue(postId);
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateIsEnabled(long membershipId, boolean isEnabled) {
    Membership existingMembership =
        this.membershipRepository
            .findById(membershipId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));
    Post existingPost =
        this.postRepository
            .findById(existingMembership.getPost().getId())
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
    if (existingMembership.getIsEnabled() == isEnabled) {
      return;
    }
    if (isEnabled && !existingPost.getIsEnabled()) {
      throw new ApiException(ErrorCode.INVALID_POST_STATUS_TO_ENABLE_MEMBERSHIP);
    }
    existingMembership.updateIsEnabled(isEnabled);
  }
}
