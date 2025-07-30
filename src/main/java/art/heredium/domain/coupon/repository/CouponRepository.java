package art.heredium.domain.coupon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import art.heredium.domain.company.entity.Company;
import art.heredium.domain.coupon.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
  List<Coupon> findByCompanyAndIsDeletedFalse(Company company);

  /**
   * 마케팅 동의 시 발급해 주어야 할 혜택용 쿠폰만 조회
   * MySQL 5.7+ 확인
   */
  List<Coupon> findByMarketingConsentBenefitTrue();

  @Modifying
  @Query("DELETE FROM Coupon c WHERE c.company.id = :companyId")
  void deleteByCompanyId(@Param("companyId") Long companyId);

  @Query(value =
          "SELECT * FROM coupon " +
                  "WHERE is_recurring = true " +
                  "  AND send_day_of_month = :day " +
                  // recipient_type JSON 배열 길이가 1 이고, 그 첫 번째 값이 0 인 건 제외
                  "  AND NOT (JSON_LENGTH(recipient_type) = 1 " +
                  "       AND JSON_EXTRACT(recipient_type, '$[0]') = 0)",
          nativeQuery = true
  )
  List<Coupon> findByIsRecurringTrueAndSendDayOfMonthExcludingDefault(
          @Param("day") int day);
}
