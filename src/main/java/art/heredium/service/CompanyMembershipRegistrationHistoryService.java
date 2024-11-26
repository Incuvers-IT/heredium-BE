package art.heredium.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.membership.entity.CompanyMembershipRegistrationHistory;
import art.heredium.domain.membership.entity.RegistrationStatus;
import art.heredium.domain.membership.model.dto.request.CompanyMembershipRegistrationHistoryCreateRequest;
import art.heredium.domain.membership.repository.CompanyMembershipRegistrationHistoryRepository;

@Service
@RequiredArgsConstructor
public class CompanyMembershipRegistrationHistoryService {
  private final CompanyMembershipRegistrationHistoryRepository
      companyMembershipRegistrationHistoryRepository;

  @Transactional(rollbackFor = Exception.class)
  public CompanyMembershipRegistrationHistory createMembershipRegistrationHistory(
      final CompanyMembershipRegistrationHistoryCreateRequest request) {
    return this.companyMembershipRegistrationHistoryRepository.save(
        CompanyMembershipRegistrationHistory.builder()
            .email(request.getEmail())
            .phone(request.getPhone())
            .startDate(request.getStartDate())
            .price(request.getPrice())
            .paymentDate(request.getPaymentDate())
            .status(request.getStatus())
            .failedReason(request.getFailedReason())
            .account(request.getAccount())
            .build());
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateRegistrationStatus(final List<Long> ids, final RegistrationStatus status) {
    final List<CompanyMembershipRegistrationHistory> existingHistories =
        this.companyMembershipRegistrationHistoryRepository.findAllById(ids);
    if (existingHistories.isEmpty()) {
      return;
    }
    existingHistories.forEach(
        existingHistory -> {
          if (existingHistory.getStatus() != status) {
            existingHistory.updateStatus(status);
          }
        });
    this.companyMembershipRegistrationHistoryRepository.saveAll(existingHistories);
  }
}
