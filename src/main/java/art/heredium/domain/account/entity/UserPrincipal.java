package art.heredium.domain.account.entity;

import art.heredium.oauth.provider.OAuth2Provider;
import art.heredium.domain.account.type.AuthType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    private final Admin admin;
    private final Account account;
    private final Long id;
    private final String email;
    private final String password;
    private String name;
    private final Boolean isEnabled;
    private Boolean isSleeper;
    private final OAuth2Provider provider;
    private AuthType auth;
    private Collection<GrantedAuthority> authorities;

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.getIsEnabled();
    }

    public UserPrincipal(Account account) {
        this.admin = null;
        this.account = account;
        this.id = account.getId();
        this.email = account.getEmail();
        this.password = account.getPassword();
        this.provider = account.getProviderType();
        this.isEnabled = true;
        AccountInfo accountInfo = account.getAccountInfo();
        SleeperInfo sleeperInfo = account.getSleeperInfo();
        if (accountInfo != null) {
            this.isSleeper = false;
            this.auth = accountInfo.getAuth();
            this.name = accountInfo.getName();
            this.authorities = Collections.singleton(new SimpleGrantedAuthority(accountInfo.getAuth().getRole()));
        } else if (sleeperInfo != null) {
            this.isSleeper = true;
            this.auth = sleeperInfo.getAuth();
            this.name = sleeperInfo.getName();
            this.authorities = Collections.singleton(new SimpleGrantedAuthority(sleeperInfo.getAuth().getRole()));
        }
    }

    public UserPrincipal(Admin admin) {
        this.admin = admin;
        this.account = null;
        this.id = admin.getId();
        this.email = admin.getEmail();
        this.password = admin.getPassword();
        this.provider = null;
        this.isEnabled = admin.getAdminInfo().getIsEnabled();
        this.isSleeper = false;
        this.auth = admin.getAdminInfo().getAuth();
        this.name = admin.getAdminInfo().getName();
        this.authorities = Collections.singleton(new SimpleGrantedAuthority(admin.getAdminInfo().getAuth().getRole()));
    }
}
