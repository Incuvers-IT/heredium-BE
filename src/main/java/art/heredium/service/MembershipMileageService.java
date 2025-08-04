package art.heredium.service;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipMileage;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.request.MembershipMileageCreateRequest;
import art.heredium.domain.membership.model.dto.request.MembershipMileageSearchRequest;
import art.heredium.domain.membership.model.dto.response.MembershipMileagePage;
import art.heredium.domain.membership.model.dto.response.MembershipMileageResponse;
import art.heredium.domain.membership.repository.MembershipMileageRepository;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.domain.membership.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MembershipMileageService {

  @Value("${membership.mileage.expiration-years:3}")
  private int expirationYears;

  private final MembershipRepository membershipRepository;
  private final MembershipMileageRepository membershipMileageRepository;
  private final MembershipRegistrationRepository registrationRepo;
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

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
    String currentUser = (principal != null ? principal.getName() : "SYSTEM");

    MembershipMileage mm = MembershipMileage.builder()
            .account(account)
            .type(0)  // 0: 적립
            .category(req.getCategory())
            .categoryId(req.getCategoryId())
            .paymentMethod(req.getPaymentMethod())
            .paymentAmount(req.getPaymentAmount())
            .serialNumber(req.getSerialNumber())
            .mileageAmount(req.getMileageAmount())
            .expirationDate(now.plusYears(expirationYears)) // 필요 시 만료일 계산
            .createdName(currentUser)
            .lastModifiedName(currentUser)
            .build();
    // 만료일 계산 등 추가 로직이 있으면 여기에
    membershipMileageRepository.save(mm);
  }

  @Transactional
  public void refundMileage(Long originalId, String reason, boolean upgradeCancel) {

    // 로그인 사용자명 조회 (null이면 SYSTEM)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
    String currentUser = (principal != null ? principal.getName() : "SYSTEM");

    // 0) 원본 마일리지 엔티티 조회 (존재하지 않으면 404)
    MembershipMileage orig = membershipMileageRepository.findById(originalId)
            .orElseThrow(() -> new EntityNotFoundException("Original mileage not found: " + originalId));

    // 1) 'upgradeCancel' 플래그가 true인 경우: 승급 취소 흐름
    if (upgradeCancel) {
      Long accountId = orig.getAccount().getId();

      // 1‑a) Tier2 멤버십 가입 여부 확인
      if (registrationRepo.existsByAccountIdAndMembershipCode(accountId, 2)) {
        // 1‑b) 실제 최신 가입 정보 조회
        MembershipRegistration reg = registrationRepo
                .findLatestForAccount(accountId)
                .orElseThrow(() -> new EntityNotFoundException("No registration found"));

        // 1‑c) Tier1(기본, code=1)으로 다운그레이드 후 만료일 해제
        Membership basic = membershipRepository.findByCode(1)
                .orElseThrow(() -> new EntityNotFoundException("Basic membership not found"));

        reg.setMembership(basic);
        reg.setExpirationDate(null);
        reg.setLastModifiedName(currentUser);
        registrationRepo.save(reg);
      }

      // 1‑d) 기존의 summary(relatedMileage) 자식 레코드들 연결 해제
      MembershipMileage summary = orig.getRelatedMileage();
      if (summary != null) {
        membershipMileageRepository.markCancelledById(summary.getId(), "승급 취소", currentUser);

        membershipMileageRepository
          .findByRelatedMileageId(summary.getId())
          .forEach(child -> {
            child.setRelatedMileage(null);
            membershipMileageRepository.save(child);
          });
      }

    }else{
      // 2) 일반 환불(취소) 흐름: summary(요약) 마일리지 회복
      MembershipMileage summary = orig.getRelatedMileage();
      if (summary != null) {
        int restored = summary.getMileageAmount() + orig.getMileageAmount();
        summary.setMileageAmount(restored);
        membershipMileageRepository.save(summary);
      }
    }

    // 4) 원본 엔티티를 '취소 완료(type=5)'로 마킹 및 연관 해제
    orig.setType(5);
    orig.setRelatedMileage(null);
    orig.setLastModifiedName(currentUser);
    membershipMileageRepository.save(orig);

    // 5) 차감(취소) Adjustment 레코드 생성 (type=3)
    createAdjustmentMileage(
            orig,
            3,  // 3: 소멸(취소)
            reason
    );
  }

  /**
   * 멤버십 승급 이벤트를 마일리지 테이블에 남깁니다.
   * @param accountId  승급된 회원 계정 ID
   * @param requiredScore  멤버십2 승급스코어
   */
  public void createUpgradeMileage(Long accountId, int requiredScore, String tier2Name) {
    Account acct = accountRepo.findById(accountId)
            .orElseThrow(() -> new EntityNotFoundException("Account not found"));

    MembershipMileage mm = MembershipMileage.builder()
            .account(acct)
            .type(1)                      // 2: 승급 이벤트
            .category(null)               // 필요시 적절한 카테고리 값 지정
            .categoryId(null)            // 해당 없으면 null
            .paymentMethod(0)            // 온라인/오프라인 구분 없으면
            .paymentAmount(0)
            .serialNumber(null)          // 일련번호 없으면 null
            .mileageAmount(-requiredScore) // 증감 마일리지(없으면 0)
            .expirationDate(null)        // 이벤트용이므로 만료일 없음
            .remark(tier2Name)
            .relatedMileage(null)
            .build();

    mm.setCreatedName("SYSTEM");
    mm.setLastModifiedName("SYSTEM");

    membershipMileageRepository.save(mm);
  }

  /**
   * 승급 시 ‘요약(summary)’ 레코드(–requiredScore)와
   * FIFO 원본(type=0)마다 ‘child’ 차감(–consume) 레코드를 생성합니다.
   *
   * @param accountId      대상 계정 ID
   * @param requiredScore  차감할 총점수 (예: 70)
   * @param tierName       승급 티어 이름 (알림·remark용)
   */
  public void createLinkedUpgradeMileage(Long accountId,
                                         int requiredScore,
                                         String tierName) {
    Account acct = accountRepo.findById(accountId)
            .orElseThrow(() -> new EntityNotFoundException("Account not found"));

    // 1) summary 레코드 생성
    MembershipMileage summary = MembershipMileage.builder()
            .account(acct)
            .type(1)                               // 1: 승급 이벤트
            .paymentMethod(0)
            .paymentAmount(0)                     // 이력용
            .mileageAmount(-requiredScore)        // ex: -70
            .remark(tierName + " 승급")
            .relatedMileage(null)                 // summary는 관련 대상 없음
            .build();

    summary.setCreatedName("SYSTEM");
    summary.setLastModifiedName("SYSTEM");
    
    membershipMileageRepository.save(summary);

    // 2) FIFO로 원본(type=0) 조회(만료일 오름차순)
    int remain = requiredScore;
    List<MembershipMileage> accruals = membershipMileageRepository
            .findByAccountIdAndTypeAndRelatedMileageIsNullOrderByExpirationDateAsc(accountId, 0);

    for (MembershipMileage orig : accruals) {
      if (remain <= 0) break;

      // 이미 소진된 child가 있을 경우, orig.getAvailable() 형태로 처리 로직 보완 필요
      int available = orig.getMileageAmount();
      int consume  = Math.min(available, remain);
      remain -= consume;

      orig.setRelatedMileage(summary);
      membershipMileageRepository.save(orig);

      // 3) child 차감 레코드
//      MembershipMileage child = MembershipMileage.builder()
//              .account(acct)
//              .type(7)                            // summary와 동일하게 '승급'
//              .paymentMethod(0)
//              .paymentAmount(0)                   // 이력용
//              .mileageAmount(-consume)            // –30, –30, –10
//              .remark("승급 차감")
//              .relatedMileage(summary)            // 원본 +30/+30/+15
//              .build();

//      child.setCreatedName("SYSTEM");
//      child.setLastModifiedName("SYSTEM");
//      membershipMileageRepository.save(child);
    }

    if (remain > 0) {
      throw new IllegalStateException("마일리지 부족: 잔여 " + remain + "점");
    }
  }

  /**
   * 공통: adjustment(취소/만료/환불 등)용 마일리지 레코드를 생성해 저장합니다.
   *
   * @param original       원본 마일리지 엔티티
   * @param type           이벤트 타입 코드(2: 만료, 3: 환불, ...)
   * @param remark         메모
   */
  public void createAdjustmentMileage(
          MembershipMileage original,
          int type,
          String remark
  ) {
    MembershipMileage adj = MembershipMileage.builder()
            .account(original.getAccount())
            .type(type)
            .category(original.getCategory())
            .categoryId(original.getCategoryId())
            .paymentMethod(original.getPaymentMethod())
            .paymentAmount(-original.getPaymentAmount())
            .serialNumber(original.getSerialNumber())
            .mileageAmount(-original.getMileageAmount())
            .expirationDate(null)          // 이벤트용이므로 만료일 없음
            .remark(remark)
            .relatedMileage(original)      // 원본 참조
            .build();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
    String currentUser = (principal != null ? principal.getName() : "SYSTEM");

    adj.setCreatedName(currentUser);
    adj.setLastModifiedName(currentUser);

    membershipMileageRepository.save(adj);
  }

  /**
   * 승급 취소 가능 여부 체크
   */
  public boolean canCancelUpgrade(Long accountId, Long relatedMileageId, int mileageAmount) {
    // 1) Tier2(code=2) 멤버십 등록 여부
    // 1) 실제 “멤버십 가입” 테이블에서 Tier2(code=2) 가입 여부 확인
    if (!registrationRepo.existsByAccountIdAndMembershipCode(accountId, 2)) {
      return false;
    }

    // 2) 관련된 모든 차감·환불 등 이력 합계
    Integer sum = membershipMileageRepository.sumByRelatedMileageId(relatedMileageId);
    if (sum == null) sum = 0;

    // 3) Tier2 멤버십의 usageThreshold 조회
    Membership tier2 = membershipRepository
            .findByCode(2)
            .orElseThrow(() -> new EntityNotFoundException("Tier2 멤버십 없음"));
    int threshold = tier2.getUsageThreshold();

    // 4) threshold > (sum - 현재 선택 항목)
    return threshold > (sum - mileageAmount);
  }

  public MembershipMileagePage getMembershipsMileageListWithTotal(MembershipMileageSearchRequest request, Pageable pageable) {

//    Page<MembershipMileageResponse> page = membershipMileageRepository.getMembershipsMileageList(request, pageable);
    Page<MembershipMileageResponse> page = membershipMileageRepository.getUserMembershipsMileageList(request, pageable);
    long totalMileage = membershipMileageRepository.sumActiveMileageByAccount(request.getAccountId());

    LocalDate threshold = LocalDate.now().plusMonths(1);


    LocalDateTime thresholdDateTime = threshold.atTime(LocalTime.MAX);


    long expiringMileage = membershipMileageRepository
            .sumExpiringMileage(request.getAccountId(), thresholdDateTime);

    return new MembershipMileagePage(
            page.getContent(),
            page.getTotalElements(),
            totalMileage,
            expiringMileage,
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast());
  }
}
