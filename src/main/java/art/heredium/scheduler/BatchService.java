package art.heredium.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// BatchService
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

  private final StepTxService step;

  public void runAll() {
    // 1) 만료된 티켓 처리 및 해당 티켓 기준 마일리지 적립
    runSafely("step1", step::expireTicketsAndAccrueMileage);
    // 2) 만료된 2·3등급 처리 (2등급은 Retention 기준 충족 시 연장, 미충족/3등급은 1등급으로 강등)
    runSafely("step2", step::processExpiredTier2And3);
    // 3) 1등급 중 기준점수 이상 승급 처리 (만료일: 1년 뒤 전날 23:59:59)
    runSafely("step3", step::upgradeToMembership2);
    // 4) 만료 3/2/1개월 전 사전 알림톡 예약 (Tier2 & mileage 부족 대상)
    runSafely("step4", step::scheduleTierExpiryAlimTalk);
    // 5) 유효기간 경과 마일리지 소멸 처리 및 차감 이력 생성
    runSafely("step5", step::expireMileagePoints);
  }

  private void runSafely(String name, Runnable r) {
    try { r.run(); log.info("{} ok", name); }
    catch (Exception e) { log.error("{} failed", name, e); }
  }

}
