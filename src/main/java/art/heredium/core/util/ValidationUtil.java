package art.heredium.core.util;

import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.coupon.model.dto.request.CouponCreateRequest;
import art.heredium.ncloud.bean.CloudStorage;

public class ValidationUtil {

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

  public static void validateImage(CloudStorage cloudStorage, String imageUrl) {
    if (StringUtils.isNotEmpty(imageUrl) && !cloudStorage.isExistObject(imageUrl)) {
      throw new ApiException(ErrorCode.S3_NOT_FOUND, imageUrl);
    }
  }
}
