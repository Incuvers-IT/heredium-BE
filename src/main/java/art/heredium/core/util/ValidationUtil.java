package art.heredium.core.util;

import lombok.NonNull;

import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.excel.constants.ExcelConstant;
import art.heredium.ncloud.bean.CloudStorage;

public class ValidationUtil {

  public static void validateImage(CloudStorage cloudStorage, String imageUrl) {
    if (StringUtils.isNotEmpty(imageUrl) && !cloudStorage.isExistObject(imageUrl)) {
      throw new ApiException(ErrorCode.S3_NOT_FOUND, imageUrl);
    }
  }

  public static void validateExcelExtension(@NonNull MultipartFile multipartFile) {
    final String fileExtension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
    if (!ExcelConstant.XLS.equalsIgnoreCase(fileExtension)
        && !ExcelConstant.XLSX.equalsIgnoreCase(fileExtension)) {
      throw new ApiException(ErrorCode.INVALID_EXCEL_FILE, "Uploaded file should be .xlsx");
    }
  }
}
