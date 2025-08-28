package art.heredium.domain.membership.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.entity.RegistrationType;

@Repository
public interface MembershipRegistrationRepository
    extends JpaRepository<MembershipRegistration, Long>, MembershipRegistrationRepositoryQueryDsl {

  @Query(
      value =
          "SELECT mr from MembershipRegistration mr WHERE mr.account.id = :accountId"
              + " AND mr.paymentStatus = art.heredium.domain.membership.entity.PaymentStatus.COMPLETED")
  Optional<MembershipRegistration> findCompletedOneByAccountIdAndNotExpired(
      @Param("accountId") Long accountId);

  @Query(
      "SELECT mr FROM MembershipRegistration mr WHERE mr.account.id IN :accountIds "
          + "AND mr.registrationDate = (SELECT MAX(mr2.registrationDate) FROM MembershipRegistration mr2 WHERE mr2.account = mr.account)")
  List<MembershipRegistration> findLatestForAccounts(@Param("accountIds") List<Long> accountIds);

  @Query(
      "SELECT mr FROM MembershipRegistration mr WHERE mr.account.id = :accountId "
          + "AND mr.registrationDate = (SELECT MAX(mr2.registrationDate) FROM MembershipRegistration mr2 WHERE mr2.account = mr.account)")
  Optional<MembershipRegistration> findLatestForAccount(@Param("accountId") Long accountId);

  Optional<MembershipRegistration> findTopByAccountOrderByRegistrationDateDesc(Account account);

//  Optional<MembershipRegistration> findByPaymentOrderId(String orderId);

  List<MembershipRegistration> findByPaymentStatusInAndCreatedDateBefore(
      List<PaymentStatus> paymentStatuses, LocalDateTime dateTime);

  List<MembershipRegistration> findByAccountIdAndPaymentStatus(
      long accountId, PaymentStatus paymentStatus);

  List<MembershipRegistration> findByExpirationDateBeforeAndPaymentStatusNotIn(
      LocalDateTime date, List<PaymentStatus> statuses);

  Optional<MembershipRegistration>
      findByAccountIdAndRegistrationTypeAndPaymentStatusAndExpirationDateAfter(
          Long accountId,
          RegistrationType registrationType,
          PaymentStatus paymentStatus,
          LocalDateTime date);

  /**
   * code=1(기본) 이면서 완료(COMPLETED) 상태인 모든 최신 등록 건 조회
   */
  @Query(
          "select mr " +
          "from MembershipRegistration mr " +
          "join mr.membership m " +
          "where m.code = :code " +
          " and mr.paymentStatus = art.heredium.domain.membership.entity.PaymentStatus.COMPLETED " +
          " and mr.account.id > 4999"
  )
  List<MembershipRegistration> findCompletedByMembershipCode(@Param("code") int code);

  @Query(
          "select mr " +
          "from MembershipRegistration mr " +
          "join mr.membership m " +
          "where m.code = 1 " +
          "  and mr.paymentStatus = art.heredium.domain.membership.entity.PaymentStatus.COMPLETED " +
          "  and (select coalesce(sum(mm.mileageAmount), 0) " +
          "         from MembershipMileage mm " +
          "        where mm.account     = mr.account " +
          "          and mm.type        = 0" +
          "          AND mm.relatedMileage IS NULL " +
          "      ) >= :threshold"
  )
  List<MembershipRegistration> findTier1WithMinMileage(
          @Param("threshold") long threshold
  );

  /**
   * 2·3등급(membership.code)이면서, expirationDate가 now 이전인 레코드를 조회
   *
   * @param codes 조회할 등급 코드 리스트 (예: List.of(2,3))
   * @param now   비교 기준일시
   * @return 만료된 2·3등급 MembershipRegistration 목록
   */
  @Query("SELECT mr FROM MembershipRegistration mr " +
          "WHERE mr.membership.code IN :codes " +
          "  AND mr.expirationDate < :now")
  List<MembershipRegistration> demoteExpiredToBasic(
          @Param("codes") List<Integer> codes,
          @Param("now") LocalDateTime now
  );

  @Query(
          "SELECT mr " +
                  "  FROM MembershipRegistration mr " +
                  "  LEFT JOIN MembershipMileage mm ON mm.account.id = mr.account.id " +
                  "   AND mm.type       = 0 " +
                  "   AND mm.relatedMileage IS NULL " +
                  " WHERE mr.membership.code      = 2 " +
                  "   AND mr.expirationDate >= :start " +
                  "   AND mr.expirationDate <  :end " +
                  " GROUP BY mr " +
                  "HAVING COALESCE(SUM(mm.mileageAmount), 0) < :threshold"
  )
  List<MembershipRegistration> findTier2ExpiringWithMileageBelow(
          @Param("start")     LocalDateTime start,
          @Param("end")       LocalDateTime end,
          @Param("threshold") long threshold
  );

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE MembershipRegistration mr " +
          "SET mr.isDeleted = true, mr.lastModifiedDate = CURRENT_TIMESTAMP " +
          "WHERE mr.company.id = :companyId AND mr.isDeleted = false")
  void softDeleteAllByCompanyId(@Param("companyId") Long companyId);

  /**
   * account.id, membership.code 기준으로 가입 여부 확인
   */
  @Query(
          "SELECT CASE WHEN COUNT(mr) > 0 THEN true ELSE false END " +
                  "FROM MembershipRegistration mr " +
                  "JOIN mr.membership m " +
                  "WHERE mr.account.id = :accountId " +
                  "  AND m.code        = :membershipCode"
  )
  boolean existsByAccountIdAndMembershipCode(
          @Param("accountId") Long accountId,
          @Param("membershipCode") int membershipCode
  );

  /**
   * paymentStatus 조건 없이 membership.code 만으로 조회합니다.
   */
  @Query(
          "SELECT mr " +
          "  FROM MembershipRegistration mr " +
          " JOIN mr.membership m " +
          " WHERE m.code = :membershipCode"
  )
  List<MembershipRegistration> findByMembershipCode(
          @Param("membershipCode") int membershipCode
  );

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE MembershipRegistration mr " +
          "SET mr.isDeleted = true, mr.lastModifiedDate = CURRENT_TIMESTAMP " +
          "WHERE mr.account.id = :accountId AND mr.isDeleted = false")
  int softDeleteByAccountId(@Param("accountId") Long accountId);

}
