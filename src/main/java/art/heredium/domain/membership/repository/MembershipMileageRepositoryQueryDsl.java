package art.heredium.domain.membership.repository;

import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.request.MembershipMileageSearchRequest;
import art.heredium.domain.membership.model.dto.response.MembershipMileageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MembershipMileageRepositoryQueryDsl {
  Page<MembershipMileageResponse> getMembershipsMileageList(
      GetAllActiveMembershipsRequest request, Pageable pageable);

  Page<MembershipMileageResponse> getUserMembershipsMileageList(
          MembershipMileageSearchRequest request, Pageable pageable);
}
