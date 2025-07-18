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
   */
  List<Coupon> findByMarketingConsentBenefitTrue();

  @Modifying
  @Query("DELETE FROM Coupon c WHERE c.company.id = :companyId")
  void deleteByCompanyId(@Param("companyId") Long companyId);
}
