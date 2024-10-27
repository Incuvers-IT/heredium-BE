package art.heredium.excel.constants;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;

@AllArgsConstructor
@Getter
public enum UploadedMembershipRegistrationColumns {
  TITLE(0, "제목"),
  EMAIL(1, "이메일"),
  PHONE(2, "핸드폰"),
  START_DATE(3, "시작 날짜"),
  PRICE(4, "가격"),
  PAYMENT_DATE(5, "지불 날짜"),
  NAME(6, "이름"),
  ;

  private final int columnIndex;
  private final String columnName;

  public static String getColumnNameByIndex(int columnIndex) {
    return Arrays.stream(UploadedMembershipRegistrationColumns.values())
        .filter(col -> col.getColumnIndex() == columnIndex)
        .map(UploadedMembershipRegistrationColumns::getColumnName)
        .findFirst()
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.INVALID_EXCEL_COLUMNS,
                    "Column names should be ['제목', '이메일', '핸드폰', '시작 날짜', '가격', '지불 날짜', '이름']"));
  }
}
