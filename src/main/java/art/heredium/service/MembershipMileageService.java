package art.heredium.service;

import art.heredium.core.config.properties.HerediumProperties;
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
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MembershipMileageService {

  @Value("${membership.mileage.expiration-days:364}")
  private int expirationDays;

  private final MembershipRepository membershipRepository;
  private final CouponUsageService couponUsageService;
  private final MembershipMileageRepository membershipMileageRepository;
  private final MembershipRegistrationRepository registrationRepo;
  private final AccountRepository accountRepo;
  // ↓ 알림톡/환경정보
  private final HerediumAlimTalk alimTalk;
  private final HerediumProperties herediumProperties;

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
            .expirationDate(calcExpireAt(now)) // 필요 시 만료일 계산
            .createdName(currentUser)
            .lastModifiedName(currentUser)
            .build();
    // 만료일 계산 등 추가 로직이 있으면 여기에
    membershipMileageRepository.save(mm);
  }

  /**
   * 티켓 만료/환불 등 티켓 연동 적립 생성
   * @param accountId      계정 ID
   * @param points         적립 포인트 (1,000원당 1점)
   * @param paymentMethod  0:온라인, 1:오프라인
   * @param paymentAmount  순결제금액(net)
   * @param desc           비고(예: "티켓 만료 적립 - 전시명")
   * @param ticket         연동 티켓 엔티티(FK)
   */
  public MembershipMileage earnFromTicket(
          Long accountId,
          int points,
          int paymentMethod,
          int paymentAmount,
          String desc,
          Ticket ticket
  ) {

    if (ticket != null && membershipMileageRepository.existsByTicket_IdAndType(ticket.getId(), 0)) {
      // 이미 이 티켓으로 적립된 기록이 있으면 스킵
      return null;
    }

    Account account = accountRepo.findById(accountId)
            .orElseThrow(() -> new EntityNotFoundException("Account not found"));

    LocalDateTime now = Constants.getNow();

    // 현재 사용자명(없으면 SYSTEM)
    String currentUser = "SYSTEM";
    try {
      Object auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (auth instanceof UserPrincipal) {
        currentUser = ((UserPrincipal) auth).getName();
      }
    } catch (Exception ignored) {}

    String uuid = ticket.getUuid();
    String serialLast5 = (uuid == null)
            ? null
            : uuid.substring(Math.max(0, uuid.length() - 5)); // 길이<5면 전체 반환

    MembershipMileage mm = MembershipMileage.builder()
            .account(account)
            .type(0)                         // 0: 적립
            .category(ticket.getKind())      // 필요 시 카테고리 지정
            .categoryId(ticket.getKindId())
            .paymentMethod(paymentMethod)
            .paymentAmount(paymentAmount)
            .serialNumber(serialLast5)
            .mileageAmount(points)
            .expirationDate(calcExpireAt(now))
            .remark(desc)
            .relatedMileage(null)
            .build();

    // 티켓 FK 연동
    mm.setTicket(ticket);

    // created/modified
    mm.setCreatedName(currentUser);
    mm.setLastModifiedName(currentUser);

    // (선택) 중복방지 컬럼이 엔티티에 있다면 세팅
    // mm.setSource(source);
    // mm.setRelatedId(relatedId);

    return membershipMileageRepository.save(mm);
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

        // from/to 등급명 준비 (알림톡용)
        String fromName = membershipRepository.findByCode(2)
                .map(Membership::getName).orElse("상위등급");

        // 1‑c) Tier1(기본, code=1)으로 다운그레이드 후 만료일 해제
        Membership basic = membershipRepository.findByCode(1)
                .orElseThrow(() -> new EntityNotFoundException("Basic membership not found"));

        String toName = basic.getName();

        reg.setMembership(basic);
        reg.setExpirationDate(null);
        reg.setLastModifiedName(currentUser);
        registrationRepo.save(reg);

        // ▼▼ 강등 후 멤버십 쿠폰 발급 (Tier1 기준)
        couponUsageService.distributeCouponsForMembership(
                reg.getAccount(), // or orig.getAccount()
                reg,              // membership=1 로 저장된 최신 Registration
                false,            // 알림톡 즉시발송 여부
                null              // 예약시간 (필요시 now.plusMinutes(…) 등)
        );

        // ✅ 강등 알림톡(개별 전송)
        try {
          String toPhone = reg.getAccount().getAccountInfo().getPhone();
          if (toPhone != null && !toPhone.isEmpty()) {
            Map<String, String> params = new HashMap<>();
            params.put("name", reg.getAccount().getAccountInfo().getName());
            params.put("membershipNameFrom", fromName);
            params.put("membershipNameTo", toName);
            params.put("CSTel", herediumProperties.getTel());
            params.put("CSEmail", herediumProperties.getEmail());

            // 즉시 발송(null). 예약 발송하려면 LocalDate.now().atTime(10, 0) 전달
            alimTalk.sendAlimTalk(
                    toPhone,
                    params,
                    AlimTalkTemplate.MEMBERSHIP_TIER_REFUND,
                    null
            );
            log.info("[AlimTalk][REFUND 2→1] sent (accountId={})", reg.getAccount().getId());
          } else {
            log.warn("[AlimTalk][REFUND] phone missing (accountId={})", reg.getAccount().getId());
          }
        } catch (Exception e) {
          // 알림톡 실패는 트랜잭션 영향 X
          log.error("[AlimTalk][REFUND 2→1] send failed (accountId={})",
                  reg.getAccount().getId(), e);
        }
      }

      // 1‑d) 기존의 summary(relatedMileage) 자식 레코드들 연결 해제
      MembershipMileage summary = orig.getRelatedMileage();
      if (summary != null) {
//        membershipMileageRepository.markCancelledById(summary.getId(), "승급 취소", currentUser);

        // 원본 승급 기록의 마일리지 값
        int originalAmount = summary.getMileageAmount();

        membershipMileageRepository
          .findByRelatedMileageId(summary.getId())
          .forEach(child -> {
            child.setRelatedMileage(null);
            membershipMileageRepository.save(child);
          });

        // B. 취소 이력 로우 생성 (type=6, amount=0), 원본 summary를 relatedMileage로 연결해 추적 가능하게
        MembershipMileage cancelLog = MembershipMileage.builder()
                .account(summary.getAccount())
                .type(6)                         // 승급 취소
                .paymentMethod(0)
                .paymentAmount(0)
                .mileageAmount(-originalAmount)
                .remark("승급 취소")
                .relatedMileage(summary)         // 원본 summary를 참조(추적용)
                .createdName(currentUser)
                .lastModifiedName(currentUser)
                .build();
        membershipMileageRepository.save(cancelLog);
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

    // 원본 널 안전 처리 (히스토리 데이터에 널 있을 수 있음)
    Integer origPaymentAmount = Optional.ofNullable(original.getPaymentAmount()).orElse(0);
    Integer origMileageAmount = Optional.ofNullable(original.getMileageAmount()).orElse(0);
    Integer origPaymentMethod = Optional.ofNullable(original.getPaymentMethod()).orElse(0);

    MembershipMileage adj = MembershipMileage.builder()
            .account(original.getAccount())
            .type(type)
            .category(original.getCategory())
            .categoryId(original.getCategoryId())
            .paymentMethod(origPaymentMethod)                // not null
            .paymentAmount(-origPaymentAmount)              // not null
            .serialNumber(original.getSerialNumber())
            .mileageAmount(-origMileageAmount)              // not null
            .expirationDate(null)                           // 이벤트용
            .remark(remark)
            .relatedMileage(original)
            // .ticket(original.getTicket())  // 필요하면 연결 유지, 불필요하면 생략
            .build();

    // 인증 컨텍스트 안전 처리 (스케줄러/비동기 스레드)
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUser = "SYSTEM";
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
      currentUser = ((UserPrincipal) authentication.getPrincipal()).getName();
    }

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

  @Transactional
  public void refundTicketMileageAndMaybeDemote(Ticket ticket, String reason) {
    Optional<MembershipMileage> opt = membershipMileageRepository
            .findFirstByTicket_IdAndTypeOrderByIdAsc(ticket.getId(), 0); // type=0: 적립 원본
    if (!opt.isPresent()) return;

    MembershipMileage original = opt.get();
    int refundPoints = Math.abs(original.getMileageAmount());

    boolean upgradeCancel = needDemoteAfterTicketRefund(original, refundPoints);

    refundMileage(original.getId(), reason, upgradeCancel);
  }

  @Transactional(readOnly = true)
  public boolean needDemoteAfterTicketRefund(MembershipMileage original, int refundPoints) {
    Long accountId = original.getAccount().getId();

    // 현재 등급이 2 아니면 강등 없음
    Optional<MembershipRegistration> latest = registrationRepo.findLatestForAccount(accountId);
    if (!latest.isPresent() || latest.get().getMembership().getCode() != 2) return false;

    // summary(승급) 연동이 없으면 강등 판단 자체를 하지 않음
    if (original.getRelatedMileage() == null) return false;

    // 기준 점수
    int threshold = membershipRepository.findByCode(2)
            .orElseThrow(() -> new EntityNotFoundException("Tier2 not found"))
            .getUsageThreshold();

    // 같은 summary에 묶인 적립 합계 – 이번 환불 포인트
    Long relatedId = original.getRelatedMileage().getId();
    Integer sum = membershipMileageRepository.sumByRelatedMileageId(relatedId);
    int linkedTotal = (sum == null ? 0 : sum);

    // 기준 미만이면 승급취소
    return threshold > (linkedTotal - refundPoints);
  }

  private LocalDateTime calcExpireAt(LocalDateTime base) {
    // 기준 시각의 '날짜' 기준으로 364일 뒤의 '그 날 23:59:59'
    LocalDate target = base.toLocalDate().plusDays(expirationDays);
    return LocalDateTime.of(target, LocalTime.of(23, 59, 59));
  }
}
