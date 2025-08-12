package art.heredium.domain.ticket.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.type.TicketKindType;

public interface TicketRepository extends JpaRepository<Ticket, Long>, TicketRepositoryQueryDsl {
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE Ticket t SET t.state = art.heredium.domain.ticket.type.TicketStateType.EXPIRED WHERE (t.state = art.heredium.domain.ticket.type.TicketStateType.PAYMENT OR t.state = art.heredium.domain.ticket.type.TicketStateType.ISSUANCE) AND CURRENT_TIMESTAMP > t.endDate")
  void updateExpire();

  @Query(
      "SELECT t FROM Ticket t WHERE ((:startDate IS NULL AND :endDate IS NULL) OR t.startDate BETWEEN :startDate AND :endDate) "
          + "AND t.kind = :kind "
          + "AND t.kindId = :kindId "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.USER_REFUND "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.ADMIN_REFUND "
          + "AND t.type <> art.heredium.domain.ticket.type.TicketType.INVITE")
  List<Ticket> findAllByRound(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("kind") TicketKindType kind,
      @Param("kindId") Long kindId);

  Page<Ticket> findAllByAccount_IdAndKindInOrderByCreatedDateDesc(
      Long account_id, Collection<TicketKindType> kind, Pageable pageable);

  Page<Ticket> findAllByNonUser_IdAndKindInOrderByCreatedDateDesc(
      Long non_user_id, Collection<TicketKindType> kind, Pageable pageable);

  @Query(
      "SELECT COALESCE(SUM(t.number), 0) FROM Ticket t WHERE t.usedDate BETWEEN :startDate AND :endDate AND t.state = art.heredium.domain.ticket.type.TicketStateType.USED")
  Long sumVisitNumber(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT COALESCE(SUM(t.number), 0) FROM Ticket t WHERE t.usedDate BETWEEN :startDate AND :endDate AND t.kind = :kind AND t.kindId = :kindId AND t.state = art.heredium.domain.ticket.type.TicketStateType.USED")
  Long sumVisitNumber(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("kind") TicketKindType kind,
      @Param("kindId") Long kindId);

  @Query(
      "SELECT COALESCE(SUM(t.number), 0) FROM Ticket t WHERE t.startDate BETWEEN :startDate AND :endDate AND t.kind = :kind AND t.kindId = :kindId "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.ADMIN_REFUND "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.USER_REFUND "
          + "AND t.type <> art.heredium.domain.ticket.type.TicketType.INVITE")
  Long sumBookingNumber(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("kind") TicketKindType kind,
      @Param("kindId") Long kindId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT COALESCE(SUM(t.number), 0) FROM Ticket t WHERE t.kind = :kind AND t.kindId = :kindId AND t.roundId = :roundId "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.ADMIN_REFUND "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.USER_REFUND "
          + "AND t.type <> art.heredium.domain.ticket.type.TicketType.INVITE")
  Long sumBookingNumber(
      @Param("kind") TicketKindType kind,
      @Param("kindId") Long kindId,
      @Param("roundId") Long roundId);

  @Query(
      "SELECT COALESCE(SUM(t.price), 0) FROM Ticket t WHERE t.createdDate BETWEEN :startDate AND :endDate "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.ADMIN_REFUND "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.USER_REFUND")
  Long sumPrice(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT COALESCE(SUM(t.price), 0) FROM Ticket t WHERE t.startDate BETWEEN :startDate AND :endDate "
          + "AND t.kind = :kind AND t.kindId = :kindId "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.ADMIN_REFUND "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.USER_REFUND")
  Long sumPrice(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      @Param("kind") TicketKindType kind,
      @Param("kindId") Long kindId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT COALESCE(SUM(t.number), 0) FROM Ticket t "
          + "WHERE (t.account.id = :accountId OR t.nonUser.id = :nonUserId) "
          + "AND FUNCTION('DATEDIFF', t.startDate, :startDate) = 0 "
          + "AND t.kind = :kind AND t.kindId = :kindId "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.ADMIN_REFUND "
          + "AND t.state <> art.heredium.domain.ticket.type.TicketStateType.USER_REFUND "
          + "AND t.type <> art.heredium.domain.ticket.type.TicketType.INVITE")
  Long sumTicketNumber(
      @Param("accountId") Long accountId,
      @Param("nonUserId") Long nonUserId,
      @Param("startDate") LocalDateTime startDate,
      @Param("kind") TicketKindType kind,
      @Param("kindId") Long kindId);

  Ticket findByIdAndUuid(Long id, String uuid);

  Ticket findByUuid(String uuid);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE Ticket t SET t.name = '탈퇴한 계정', t.phone = '', t.email = '' WHERE t.account.id = :id")
  void terminateByAccount(@Param("id") Long id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE Ticket t SET t.name = '탈퇴한 계정', t.phone = '', t.email = '' WHERE t.account.id IN (:id)")
  void terminateByAccount(@Param("id") List<Long> id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE Ticket t SET t.name = '탈퇴한 계정', t.phone = '', t.email = '' WHERE t.nonUser.id IN (:id)")
  void terminateByNonUser(@Param("id") List<Long> id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE Ticket t SET t.name = '탈퇴한 계정', t.phone = '', t.email = '' WHERE t.nonUser.id IS NOT NULL AND t.name <> '탈퇴한 계정' AND FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, t.createdDate) >= :day")
  void terminateByNonUser(@Param("day") int day);

  /*@Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "UPDATE ticket t" +
      " INNER JOIN non_user n ON t.non_user_id = n.id" +
      " SET t.name  = '탈퇴한 계정'," +
      "    t.phone = ''," +
      "    t.email = ''" +
      " WHERE n.hana_bank_uuid IS NULL" +
      "  AND t.name <> '탈퇴한 계정'" +
      "  AND DATEDIFF(CURRENT_TIMESTAMP, t.created_date) >= :day", nativeQuery = true)
  void terminateByNonUser(@Param("day") int day);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "UPDATE ticket t" +
      " INNER JOIN non_user n ON t.non_user_id = n.id" +
      " SET t.name  = '탈퇴한 계정'," +
      "    t.phone = ''," +
      "    t.email = ''" +
      " WHERE n.hana_bank_uuid IS NOT NULL" +
      "  AND t.name <> '탈퇴한 계정'" +
      "  AND DATEDIFF(CURRENT_TIMESTAMP, t.created_date) >= :day", nativeQuery = true)
  void terminateByHanaBank(@Param("day") int day);*/

  @Query(
      "SELECT DISTINCT YEAR(t.createdDate) FROM Ticket t WHERE t.account.id = :id AND t.kind IN (:kinds) ORDER BY YEAR(t.createdDate) DESC")
  List<Integer> findTicketYear(@Param("id") Long id, @Param("kinds") List<TicketKindType> kinds);

  Optional<Ticket> findByIdAndNonUser_NameAndNonUser_PhoneAndPassword(
      Long id, String nonUser_name, String nonUser_phone, String password);

  // 적립 대상: account not null + 티켓 email not empty + 순금액 ≥ 1000 + 상태(결제/발권) + 종료 < now
  @Query("select t " +
          "from Ticket t " +
          "where (t.state = art.heredium.domain.ticket.type.TicketStateType.PAYMENT " +
          "    or t.state = art.heredium.domain.ticket.type.TicketStateType.ISSUANCE) " +
          "  and t.endDate < :now " +
          "  and t.account is not null " +
          "  and t.email is not null " +
          "  and t.email <> '' " +
          "  and (t.originPrice - COALESCE(t.couponDiscountAmount, 0)) >= 1000")
  List<Ticket> findTargetsForAccrual(@Param("now") LocalDateTime now);

  // 전체 만료(계정 유무/금액 무관)
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Ticket t " +
          "   set t.state = art.heredium.domain.ticket.type.TicketStateType.EXPIRED " +
          " where (t.state = art.heredium.domain.ticket.type.TicketStateType.PAYMENT " +
          "     or t.state = art.heredium.domain.ticket.type.TicketStateType.ISSUANCE) " +
          "   and t.endDate < :now")
  int updateExpireAll(@Param("now") LocalDateTime now);

}
