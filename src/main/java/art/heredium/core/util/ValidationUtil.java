package art.heredium.core.util;

import java.time.LocalDate;

import lombok.NonNull;

import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.post.entity.Post;
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
      throw new ApiException(ErrorCode.INVALID_EXCEL_FILE, "Uploaded file should be .xls or .xlsx");
    }
  }

  public static void validateRegistrationDate(
      @NonNull LocalDate registrationDate, @NonNull Post post) {
    if (post.getOpenDate() != null && post.getOpenDate().isAfter(registrationDate)) {
      throw new ApiException(
          ErrorCode.REGISTERING_MEMBERSHIP_IS_NOT_AVAILABLE,
          "Membership is not available for registration yet");
    }
    if (post.getStartDate().isAfter(registrationDate)
        || post.getEndDate().isBefore(registrationDate)) {
      throw new ApiException(
          ErrorCode.INVALID_REGISTRATION_DATE,
          "Registration date should be between post start date and post end date");
    }
  }
}
