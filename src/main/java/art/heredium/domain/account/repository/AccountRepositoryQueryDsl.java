package art.heredium.domain.account.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.account.model.dto.request.*;
import art.heredium.domain.account.model.dto.response.*;

public interface AccountRepositoryQueryDsl {
  Page<GetAccountTicketGroupResponse> search(GetAccountTicketGroupRequest dto, Pageable pageable);

  Page<GetAccountTicketInviteResponse> search(GetAccountTicketInviteRequest dto, Pageable pageable);

  Page<GetAdminAccountResponse> search(GetAdminAccountRequest dto, Pageable pageable);

  List<GetAdminAccountResponse> search(GetAdminAccountRequest dto);

  Page<GetAdminSleeperResponse> search(GetAdminSleeperRequest dto, Pageable pageable);

  List<GetAdminSleeperResponse> search(GetAdminSleeperRequest dto);

  Page<AccountWithMembershipInfoResponse> searchWithMembershipInfo(
      GetAccountWithMembershipInfoRequest dto, Pageable pageable);

  Page<AccountWithMembershipInfoIncludingTitleResponse> searchWithMembershipInfoIncludingTitle(
      GetAccountWithMembershipInfoIncludingTitleRequest request, Pageable pageable);

  List<AccountWithMembershipInfoIncludingTitleResponse> listWithMembershipInfoIncludingTitle(
      final GetAccountWithMembershipInfoIncludingTitleRequest dto);
}
