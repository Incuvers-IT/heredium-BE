package art.heredium.domain.account.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.account.model.dto.request.GetAccountTicketGroupRequest;
import art.heredium.domain.account.model.dto.request.GetAccountTicketInviteRequest;
import art.heredium.domain.account.model.dto.request.GetAccountWithMembershipInfoRequest;
import art.heredium.domain.account.model.dto.request.GetAdminAccountRequest;
import art.heredium.domain.account.model.dto.request.GetAdminSleeperRequest;
import art.heredium.domain.account.model.dto.response.AccountWithMembershipInfoResponse;
import art.heredium.domain.account.model.dto.response.GetAccountTicketGroupResponse;
import art.heredium.domain.account.model.dto.response.GetAccountTicketInviteResponse;
import art.heredium.domain.account.model.dto.response.GetAdminAccountResponse;
import art.heredium.domain.account.model.dto.response.GetAdminSleeperResponse;

public interface AccountRepositoryQueryDsl {
  Page<GetAccountTicketGroupResponse> search(GetAccountTicketGroupRequest dto, Pageable pageable);

  Page<GetAccountTicketInviteResponse> search(GetAccountTicketInviteRequest dto, Pageable pageable);

  Page<GetAdminAccountResponse> search(GetAdminAccountRequest dto, Pageable pageable);

  List<GetAdminAccountResponse> search(GetAdminAccountRequest dto);

  Page<GetAdminSleeperResponse> search(GetAdminSleeperRequest dto, Pageable pageable);

  List<GetAdminSleeperResponse> search(GetAdminSleeperRequest dto);

  Page<AccountWithMembershipInfoResponse> searchWithMembershipInfo(
      GetAccountWithMembershipInfoRequest dto, Pageable pageable);
}
