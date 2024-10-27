package art.heredium.core.util;

import lombok.NonNull;

import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.ncloud.bean.CloudStorage;

public class ValidationUtil {

  private static final String XLSX_EXTENSION = "xlsx";

  public static void validateImage(CloudStorage cloudStorage, String imageUrl) {
    if (StringUtils.isNotEmpty(imageUrl) && !cloudStorage.isExistObject(imageUrl)) {
      throw new ApiException(ErrorCode.S3_NOT_FOUND, imageUrl);
    }
  }

  public static void validateExcelExtension(@NonNull MultipartFile multipartFile) {
    if (!XLSX_EXTENSION.equals(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))) {
      throw new ApiException(ErrorCode.INVALID_EXCEL_FILE, "Uploaded file should be .xlsx");
    }
  }
}
