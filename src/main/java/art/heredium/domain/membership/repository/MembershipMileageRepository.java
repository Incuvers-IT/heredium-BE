package art.heredium.domain.membership.repository;

import art.heredium.domain.membership.entity.MembershipMileage;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.request.MembershipMileageSearchRequest;
import art.heredium.domain.membership.model.dto.response.MembershipMileageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MembershipMileageRepository extends JpaRepository<MembershipMileage, Long> {
  Page<MembershipMileageResponse> getMembershipsMileageList(
          GetAllActiveMembershipsRequest request, Pageable pageable);

  /**
   * accountId에 대해 type=0(적립)이고,
   * expirationDate가 없거나 현재 시각 이후인(만료되지 않은) 마일리지 합계를 반환.
   */
  @Query(
          "SELECT COALESCE(SUM(m.mileageAmount), 0) " +
                  "FROM MembershipMileage m " +
                  "WHERE m.account.id = :accountId " +
                  "  AND m.type = 0 " +
                  "  AND m.relatedMileage IS NULL "

  )
  long sumActiveMileageByAccount(@Param("accountId") Long accountId);

  /**
   * type=0(적립) 이면서 expirationDate < now 인 엔트리만 JPQL로 조회
   */
  @Query("SELECT m FROM MembershipMileage m " +
          " WHERE m.type = :type " +
          "  AND m.expirationDate < :now " +
          "  AND m.relatedMileage IS NULL "
  )
  List<MembershipMileage> findExpiredByTypeAndExpirationDateBefore(
          @Param("type") int type,
          @Param("now") LocalDateTime now
  );

  /**
   * 계정(account.id)과 이벤트 타입(type)으로 조회하여
   * relatedMileage가 NULL 이고 expirationDate 오름차순으로 정렬된 리스트를 반환합니다.
   *
   * @param accountId 계정 ID
   * @param type      이벤트 타입 (0: 적립, 1: 승급, ...)
   * @return relatedMileage IS NULL 이고 expirationDate ASC 정렬된 마일리지 목록
   */
  List<MembershipMileage> findByAccountIdAndTypeAndRelatedMileageIsNullOrderByExpirationDateAsc(
          Long accountId,
          Integer type
  );

  @Query("SELECT SUM(m.mileageAmount) FROM MembershipMileage m WHERE m.relatedMileage.id = :relatedId")
  Integer sumByRelatedMileageId(@Param("relatedId") Long relatedId);

  /**
   * 주어진 relatedMileage.id(요약(summary) ID)를 가진 모든 마일리지 레코드를 조회
   */
  @Query("SELECT m FROM MembershipMileage m WHERE m.relatedMileage.id = :relatedId")
  List<MembershipMileage> findByRelatedMileageId(@Param("relatedId") Long relatedMileageId);

  /**
   * 주어진 summary(MembershipMileage)와 연관된 모든 마일리지 레코드를 삭제
   */
  @Modifying
  @Query("DELETE FROM MembershipMileage m WHERE m.id = :summary")
  void deleteByRelatedMileage(@Param("summary") MembershipMileage summary);

  /**
   * 단일 마일리지 레코드를 “승급 취소” 상태(type=6, mileageAmount=0)로 업데이트
   */
  @Modifying
  @Query(
          "UPDATE MembershipMileage m " +
                  "   SET m.type             = 6, " +
                  "       m.mileageAmount    = 0, " +
                  "       m.remark           = :remark, " +
                  "       m.lastModifiedName = :modifier, " +
                  "       m.lastModifiedDate = CURRENT_TIMESTAMP " +
                  " WHERE m.id             = :id"
  )
  void markCancelledById(
          @Param("id")       Long id,
          @Param("remark")   String remark,
          @Param("modifier") String modifier
  );

  Page<MembershipMileageResponse> getUserMembershipsMileageList(
          MembershipMileageSearchRequest request, Pageable pageable);


  @Query("SELECT COALESCE(SUM(m.mileageAmount), 0) " +
          "FROM MembershipMileage m " +
          "WHERE m.account.id = :accountId " +
          "AND m.expirationDate BETWEEN CURRENT_DATE AND :thresholdDate ")

  Long sumExpiringMileage(@Param("accountId") Long accountId, @Param("thresholdDate") LocalDateTime thresholdDate);

  // ticket_id + type(0: 적립) 존재 여부
  boolean existsByTicket_IdAndType(Long ticketId, Integer type);

  // MembershipMileageRepository
  Optional<MembershipMileage> findFirstByTicket_IdAndTypeOrderByIdAsc(Long ticketId, Integer type);
}