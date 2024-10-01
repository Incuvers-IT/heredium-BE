package art.heredium.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.account.repository.AdminRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoadUserService {

  private final AccountRepository accountRepository;
  private final AdminRepository adminRepository;

  public UserDetails loadUser(Long pk) {
    Account account = accountRepository.findById(pk).orElse(null);
    if (account == null) {
      throw new ApiException(ErrorCode.TOKEN_USER_NOT_FOUND);
    }
    if (account.getAccountInfo() == null && account.getSleeperInfo() == null) {
      throw new ApiException(ErrorCode.USER_DISABLED);
    }
    return new UserPrincipal(account);
  }

  public UserDetails loadAdmin(Long pk) {
    Admin admin = adminRepository.findById(pk).orElse(null);
    if (admin == null) {
      throw new ApiException(ErrorCode.TOKEN_USER_NOT_FOUND);
    }
    if (!admin.getAdminInfo().getIsEnabled()) {
      throw new ApiException(ErrorCode.USER_DISABLED);
    }
    return new UserPrincipal(admin);
  }
}
