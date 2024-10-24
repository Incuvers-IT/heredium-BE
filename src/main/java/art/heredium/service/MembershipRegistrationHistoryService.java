package art.heredium.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.membership.entity.MembershipRegistrationHistory;
import art.heredium.domain.membership.model.dto.request.MembershipRegistrationHistoryCreateRequest;
import art.heredium.domain.membership.repository.MembershipRegistrationHistoryRepository;

@Service
@RequiredArgsConstructor
public class MembershipRegistrationHistoryService {
  private final MembershipRegistrationHistoryRepository membershipRegistrationHistoryRepository;

  @Transactional(rollbackFor = Exception.class)
  public MembershipRegistrationHistory createMembershipRegistrationHistory(
      final MembershipRegistrationHistoryCreateRequest request) {
    return this.membershipRegistrationHistoryRepository.save(
        MembershipRegistrationHistory.builder()
            .title(request.getTitle())
            .emailOrPhone(request.getEmailOrPhone())
            .startDate(request.getStartDate())
            .price(request.getPrice())
            .paymentDate(request.getPaymentDate())
            .status(request.getStatus())
            .reason(request.getReason())
            .account(request.getAccount())
            .build());
  }
}
