package art.heredium.service;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.membership.entity.MembershipMileage;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.request.MembershipMileageCreateRequest;
import art.heredium.domain.membership.model.dto.response.MembershipMileageResponse;
import art.heredium.domain.membership.repository.MembershipMileageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MembershipMileageService {

  private final MembershipMileageRepository membershipMileageRepository;
  private final AccountRepository accountRepo;

  public Page<MembershipMileageResponse> getMembershipsMileageList(
          GetAllActiveMembershipsRequest request, Pageable pageable) {
    return this.membershipMileageRepository.getMembershipsMileageList(
            request, pageable);
  }

  @Transactional
  public void create(MembershipMileageCreateRequest req) {
    Account account = accountRepo.findById(req.getAccountId())
            .orElseThrow(() -> new EntityNotFoundException("Account not found"));

    LocalDateTime now = Constants.getNow();

    MembershipMileage mm = MembershipMileage.builder()
            .account(account)
            .type(0)  // 0: 적립
            .category(req.getCategory())
            .categoryId(req.getCategoryId())
            .paymentMethod(req.getPaymentMethod())
            .paymentAmount(req.getPaymentAmount())
            .serialNumber(req.getSerialNumber())
            .mileageAmount(req.getMileageAmount())
            .expirationDate(now.plusYears(3)) // 필요 시 만료일 계산
            .build();
    // 만료일 계산 등 추가 로직이 있으면 여기에
    membershipMileageRepository.save(mm);
  }

  @Transactional
  public void refundMileage(Long originalId, String reason) {
    MembershipMileage orig = membershipMileageRepository.findById(originalId)
            .orElseThrow(() -> new EntityNotFoundException("Original mileage not found: " + originalId));

    // 2) 원본을 '취소(4)' 로 마킹
    orig.setType(4);
    membershipMileageRepository.save(orig);

    // 새로 복제할 환불 레코드 생성
    MembershipMileage refund = MembershipMileage.builder()
            .account(orig.getAccount())
            .type(3)  // 환불 이벤트 타입
            .category(orig.getCategory())
            .categoryId(orig.getCategoryId())
            .paymentMethod(orig.getPaymentMethod())
            .paymentAmount(-orig.getPaymentAmount())
            .serialNumber(orig.getSerialNumber())
            // 마일리지는 원본의 부호를 반대로
            .mileageAmount(-orig.getMileageAmount())
            // 만료일은 환불엔 의미가 없으므로 null 로 두거나 원본과 동일하게
            .expirationDate(null)
            .remark(reason)
            .relatedMileage(orig)
            .build();

    membershipMileageRepository.save(refund);
  }
}
