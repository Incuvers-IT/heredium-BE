package art.heredium.domain.membership.repository;

import art.heredium.domain.membership.entity.MembershipMileage;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.response.MembershipMileageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipMileageRepository extends JpaRepository<MembershipMileage, Long> {
  Page<MembershipMileageResponse> getMembershipsMileageList(
          GetAllActiveMembershipsRequest request, Pageable pageable);
}
