package art.heredium.core.util;

import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.ncloud.bean.CloudStorage;

public class ValidationUtil {

  public static void validateImage(CloudStorage cloudStorage, String imageUrl) {
    if (StringUtils.isNotEmpty(imageUrl) && !cloudStorage.isExistObject(imageUrl)) {
      throw new ApiException(ErrorCode.S3_NOT_FOUND, imageUrl);
    }
  }
}
