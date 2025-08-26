package art.heredium.domain.account.model.dto.response;

import art.heredium.domain.coupon.entity.Coupon;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.AccountInfo;
import art.heredium.domain.account.model.dto.AccountMembershipRegistrationInfo;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.oauth.provider.OAuth2Provider;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GetUserAccountInfoResponse {
  private Long id;
  private String name;
  private String email;
  private OAuth2Provider provider;
  private String birthDate;
  private String gender;
  private boolean marketingPending;
  /** 추가된 필드 **/
  private String job;
  private String state;
  private String district;
  private Boolean additionalInfoAgreed;
  private Boolean isMarketingReceive;
  private Boolean isLocalResident;
  private LocalDateTime marketingAgreedDate;
  private long totalMileage;

  /** 발급된 쿠폰 목록 */
  private List<Coupon> coupons;

  @JsonProperty("account_membership_registration_info")
  private AccountMembershipRegistrationInfo accountMembershipRegistrationInfo;

  public GetUserAccountInfoResponse(Account entity, MembershipRegistration membershipRegistration) {
    AccountInfo accountInfo = entity.getAccountInfo();
    this.id = accountInfo.getId();
    this.birthDate = accountInfo.getBirthDate();
    this.gender = accountInfo.getGender();
    this.marketingPending = accountInfo.getMarketingPending();
    this.name = accountInfo.getName();
    this.email = entity.getEmail();
    this.provider = entity.getProviderType();

    // 추가 정보
    this.job                    = accountInfo.getJob();
    this.state                  = accountInfo.getState();
    this.district               = accountInfo.getDistrict();
    this.additionalInfoAgreed   = accountInfo.getAdditionalInfoAgreed();
    this.isMarketingReceive     = accountInfo.getIsMarketingReceive();
    this.isLocalResident        = accountInfo.getIsLocalResident();
    this.marketingAgreedDate    = accountInfo.getMarketingAgreedDate();

    if (membershipRegistration != null) {
      this.accountMembershipRegistrationInfo =
          new AccountMembershipRegistrationInfo(
              membershipRegistration.getId(),
              membershipRegistration.getRegistrationDate(),
              membershipRegistration.getExpirationDate());
    }
  }

  public GetUserAccountInfoResponse(Account entity, MembershipRegistration membershipRegistration, long totalMileage) {
    AccountInfo accountInfo = entity.getAccountInfo();
    this.id = accountInfo.getId();
    this.birthDate = accountInfo.getBirthDate();
    this.gender = accountInfo.getGender();
    this.marketingPending = accountInfo.getMarketingPending();
    this.name = accountInfo.getName();
    this.email = entity.getEmail();
    this.provider = entity.getProviderType();
    this.totalMileage = totalMileage;

    // 추가 정보
    this.job                    = accountInfo.getJob();
    this.state                  = accountInfo.getState();
    this.district               = accountInfo.getDistrict();
    this.additionalInfoAgreed   = accountInfo.getAdditionalInfoAgreed();
    this.isMarketingReceive     = accountInfo.getIsMarketingReceive();
    this.isLocalResident        = accountInfo.getIsLocalResident();
    this.marketingAgreedDate    = accountInfo.getMarketingAgreedDate();

    if (membershipRegistration != null) {
      this.accountMembershipRegistrationInfo =
              new AccountMembershipRegistrationInfo(
                      membershipRegistration.getId(),
                      membershipRegistration.getRegistrationDate(),
                      membershipRegistration.getExpirationDate());
    }
  }

  public GetUserAccountInfoResponse(Account entity, MembershipRegistration membershipRegistration, List<Coupon> coupons) {
    AccountInfo accountInfo = entity.getAccountInfo();
    this.id = accountInfo.getId();
    this.birthDate = accountInfo.getBirthDate();
    this.gender = accountInfo.getGender();
    this.marketingPending = accountInfo.getMarketingPending();
    this.name = accountInfo.getName();
    this.email = entity.getEmail();
    this.provider = entity.getProviderType();

    // 추가 정보
    this.job                    = accountInfo.getJob();
    this.state                  = accountInfo.getState();
    this.district               = accountInfo.getDistrict();
    this.additionalInfoAgreed   = accountInfo.getAdditionalInfoAgreed();
    this.isMarketingReceive     = accountInfo.getIsMarketingReceive();
    this.isLocalResident        = accountInfo.getIsLocalResident();
    this.marketingAgreedDate    = accountInfo.getMarketingAgreedDate();

    if (membershipRegistration != null) {
      this.accountMembershipRegistrationInfo =
              new AccountMembershipRegistrationInfo(
                      membershipRegistration.getId(),
                      membershipRegistration.getRegistrationDate(),
                      membershipRegistration.getExpirationDate());
    }

    this.coupons = coupons;
  }
}
