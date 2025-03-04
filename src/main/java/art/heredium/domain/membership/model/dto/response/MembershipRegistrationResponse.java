package art.heredium.domain.membership.model.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.CouponType;
import art.heredium.domain.coupon.entity.CouponUsage;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.RegistrationType;

@Getter
@Setter
public class MembershipRegistrationResponse {

  @JsonProperty("id")
  private long membershipRegistrationId;

  @JsonProperty("uuid")
  private String uuid;

  @JsonProperty("membership_name")
  private String membershipName;

  @JsonProperty("company_name")
  private String companyName;

  @JsonProperty("registration_date")
  private LocalDateTime registrationDate;

  @JsonProperty("expiration_date")
  private LocalDateTime expirationDate;

  @JsonProperty("coupons")
  private List<CouponCountByTypeResponse> coupons = new ArrayList<>();

  @JsonProperty("registration_type")
  private RegistrationType registrationType;

  public MembershipRegistrationResponse(
      @NonNull MembershipRegistration membershipRegistration,
      @NonNull List<CouponUsage> couponUsages) {
    this.membershipName =
        membershipRegistration.getMembership() != null
            ? membershipRegistration.getMembership().getName()
            : null;
    this.companyName =
        membershipRegistration.getCompany() != null
            ? membershipRegistration.getCompany().getName()
            : null;
    this.registrationDate = membershipRegistration.getRegistrationDate();
    this.expirationDate = membershipRegistration.getExpirationDate();
    this.membershipRegistrationId = membershipRegistration.getId();
    this.uuid = membershipRegistration.getUuid();
    for (CouponType couponType : CouponType.values()) {
      this.coupons.add(
          new CouponCountByTypeResponse(
              couponType,
              couponUsages.stream()
                  .filter(couponUsage -> couponUsage.getCoupon().getCouponType() == couponType)
                  .count()));
    }
    this.registrationType = membershipRegistration.getRegistrationType();
  }
}
