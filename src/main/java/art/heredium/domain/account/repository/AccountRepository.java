package art.heredium.domain.account.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.account.entity.Account;
import art.heredium.oauth.provider.OAuth2Provider;

public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryQueryDsl {

  @Override
  @EntityGraph(attributePaths = {"accountInfo", "sleeperInfo"})
  Optional<Account> findById(Long id);

  @Override
  @EntityGraph(attributePaths = {"accountInfo", "sleeperInfo"})
  List<Account> findAllById(Iterable<Long> longs);

  @EntityGraph(attributePaths = {"accountInfo", "sleeperInfo"})
  Account findByEmailEqualsAndProviderType(String email, OAuth2Provider providerType);

  @EntityGraph(attributePaths = {"accountInfo", "sleeperInfo"})
  Account findBySnsIdAndProviderType(String snsId, OAuth2Provider providerType);

  boolean existsAccountByEmailAndProviderType(String email, OAuth2Provider providerType);

  @Query("SELECT COUNT(a) FROM Account a WHERE a.createdDate BETWEEN :startDate AND :endDate")
  Long countSignUp(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  boolean existsByEmailAndAccountInfo_PhoneAndProviderType(
      String email, String accountInfo_phone, OAuth2Provider providerType);

  @Query(
      "SELECT a FROM Account a INNER JOIN FETCH SleeperInfo si ON a.id = si.account.id WHERE FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, si.sleepDate) >= :day")
  List<Account> findToTerminate(@Param("day") int day);

  @Query(
      "SELECT a FROM Account a INNER JOIN FETCH AccountInfo ai ON a.id = ai.id WHERE ai.phone = :phone")
  List<Account> findEmailByPhone(@Param("phone") String phone);

  List<Account> findByIdIn(@Param("ids") Set<Long> ids);

  @Query(
      value =
          "SELECT a.* FROM account a "
              + "INNER JOIN account_info ai ON a.id = ai.account_id "
              + "WHERE ai.phone = :phone "
              + "ORDER BY ai.last_login_date IS NULL, ai.last_login_date DESC "
              + "LIMIT 1",
      nativeQuery = true)
  Optional<Account> findLatestLoginAccountByPhone(@Param("phone") String phone);

  @Query(
      value =
          "SELECT a.* FROM account a "
              + "INNER JOIN account_info ai ON a.id = ai.account_id "
              + "WHERE a.email = :email "
              + "ORDER BY ai.last_login_date IS NULL, ai.last_login_date DESC "
              + "LIMIT 1",
      nativeQuery = true)
  Optional<Account> findLatestLoginAccountByEmail(@Param("email") String email);

  @Query(
      value =
          "SELECT a.* FROM account a "
              + "INNER JOIN account_info ai ON a.id = ai.account_id "
              + "WHERE a.email = :email "
              + "AND ai.phone = :phone "
              + "AND ai.name = :name "
              + "ORDER BY ai.last_login_date DESC "
              + "LIMIT 1",
      nativeQuery = true)
  Optional<Account> findLatestLoginAccountByEmailAndPhoneAndName(
      @Param("email") String email, @Param("phone") String phone, @Param("name") String name);

  /** 마케팅 수신 동의(true)한 회원만 조회 */
  List<Account> findByAccountInfo_IsMarketingReceiveTrue();

  /** 마케팅 수신 미동의(false)한 회원만 조회 */
  List<Account> findByAccountInfo_IsMarketingReceiveFalse();

}
