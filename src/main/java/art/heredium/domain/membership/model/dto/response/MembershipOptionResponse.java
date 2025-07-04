package art.heredium.domain.membership.model.dto.response;

import art.heredium.domain.coupon.entity.CouponType;
import art.heredium.domain.coupon.entity.CouponUsage;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.RegistrationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class MembershipOptionResponse {
  private long id;
  private String name;
}
