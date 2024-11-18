package art.heredium.domain.coupon.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponSource;
import art.heredium.domain.coupon.entity.CouponUsage;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

  @Query(
      "SELECT cu FROM CouponUsage cu "
          + "INNER JOIN Coupon c "
          + "ON cu.coupon.id = c.id "
          + "WHERE cu.account.id = :accountId "
          + "AND cu.isUsed IS NOT TRUE "
          + "AND cu.expirationDate >= CURRENT_TIMESTAMP "
          + "AND c.fromSource = :source")
  List<CouponUsage> findByAccountIdAndIsUsedFalseAndNotExpiredAndSource(
      @Param("accountId") Long accountId, @Param("source") CouponSource source);

  @Query("SELECT DISTINCT cu.coupon FROM CouponUsage cu WHERE cu.account.id = :accountId")
  List<Coupon> findDistinctCouponsByAccountId(@Param("accountId") Long accountId);

  List<CouponUsage> findByMembershipRegistrationIdAndIsUsedTrue(
      @Param("membershipRegistrationId") Long membershipRegistrationId);

  List<CouponUsage> findByAccountIdAndCouponIdAndIsUsedTrue(
      @Param("accountId") Long accountId, @Param("couponId") Long couponId);

  @Query(
      "SELECT cu FROM CouponUsage cu "
          + "WHERE cu.account.id = :accountId "
          + "AND cu.coupon.id = :couponId "
          + "AND (cu.isUsed = false OR cu.isPermanent = true)")
  List<CouponUsage> findUnusedOrPermanentCoupons(
      @Param("accountId") Long accountId, @Param("couponId") Long couponId);

  Optional<CouponUsage> findByUuid(String uuid);

  List<CouponUsage> deleteByMembershipRegistrationId(
      @Param("membershipRegistrationId") Long membershipRegistrationId);
}
