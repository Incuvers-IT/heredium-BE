package art.heredium.domain.membership.repository;

import java.util.List;

import art.heredium.domain.membership.model.dto.response.ActiveMembershipDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipRegistrationsResponse;

public interface MembershipRegistrationRepositoryQueryDsl {
  Page<ActiveMembershipRegistrationsResponse> getAllActiveMembershipRegistrations(
      GetAllActiveMembershipsRequest request, Pageable pageable);

  Page<ActiveMembershipDetailResponse> getActiveMembershipRegistrations(
          GetAllActiveMembershipsRequest request, Pageable pageable);

  List<ActiveMembershipRegistrationsResponse> listActiveMembershipRegistrations(
      GetAllActiveMembershipsRequest request);
}
