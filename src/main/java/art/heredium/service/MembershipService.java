package art.heredium.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.repository.MembershipRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MembershipService {

  private final MembershipRepository membershipRepository;

  public List<Membership> findByPostIdAndIsEnabledTrue(long postId) {
    return this.membershipRepository.findByPostIdAndIsEnabledTrue(postId);
  }
}
