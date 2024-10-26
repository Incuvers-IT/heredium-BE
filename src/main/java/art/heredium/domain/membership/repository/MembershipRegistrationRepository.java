package art.heredium.domain.membership.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;

@Repository
public interface MembershipRegistrationRepository
    extends JpaRepository<MembershipRegistration, Long>, MembershipRegistrationRepositoryQueryDsl {

  @Query(
      value =
          "SELECT mr from MembershipRegistration mr WHERE mr.account.id = :accountId AND mr.expirationDate >= CURRENT_TIMESTAMP")
  Optional<MembershipRegistration> findByAccountIdAndNotExpired(@Param("accountId") Long accountId);

  @Query(
      "SELECT mr FROM MembershipRegistration mr WHERE mr.account.id IN :accountIds "
          + "AND mr.registrationDate = (SELECT MAX(mr2.registrationDate) FROM MembershipRegistration mr2 WHERE mr2.account = mr.account)")
  List<MembershipRegistration> findLatestForAccounts(@Param("accountIds") List<Long> accountIds);

  @Query(
      "SELECT mr FROM MembershipRegistration mr WHERE mr.account.id = :accountId "
          + "AND mr.registrationDate = (SELECT MAX(mr2.registrationDate) FROM MembershipRegistration mr2 WHERE mr2.account = mr.account)")
  Optional<MembershipRegistration> findLatestForAccount(@Param("accountId") Long accountId);

  Optional<MembershipRegistration> findTopByAccountOrderByRegistrationDateDesc(Account account);

  Optional<MembershipRegistration> findByAccountAndExpirationDateAfter(
      Account account, LocalDate date);

  Optional<MembershipRegistration> findByPaymentOrderId(String orderId);

  List<MembershipRegistration> findByPaymentStatusAndCreatedDateIsBefore(
      PaymentStatus paymentStatus, LocalDateTime dateTime);
}
