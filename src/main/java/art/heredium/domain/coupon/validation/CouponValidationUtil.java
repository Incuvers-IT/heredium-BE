package art.heredium.domain.coupon.validation;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.coupon.model.dto.request.CouponCreateRequest;

public class CouponValidationUtil {

  public static void validateCouponRequest(CouponCreateRequest couponRequest) {
    if ((!couponRequest.getIsPermanent() && couponRequest.getNumberOfUses() == null)
        || (couponRequest.getIsPermanent() && couponRequest.getNumberOfUses() != null)) {
      throw new ApiException(
          ErrorCode.BAD_VALID,
          String.format(
              "Invalid coupon request for '%s': If 'isPermanent' is false, 'numberOfUses' must be provided. If 'isPermanent' is true, 'numberOfUses' must not be provided.",
              couponRequest.getName()));
    }
  }
}
