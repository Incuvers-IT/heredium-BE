package art.heredium.excel.constants;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UploadedMembershipRegistrationColumns {
  TITLE(0, "제목"),
  EMAIL(1, "이메일"),
  PHONE(2, "핸드폰"),
  START_DATE(3, "시작 날짜 (YYYY-MM-DD)"),
  PRICE(4, "가격"),
  PAYMENT_DATE(5, "지불 날짜 (YYYY-MM-DD)"),
  NAME(6, "이름"),
  ;

  private final int columnIndex;
  private final String columnName;

  public static String getColumnNameByIndex(int columnIndex) {
    if (columnIndex < 0 || columnIndex > 6) {
      throw new IndexOutOfBoundsException(
          "Invalid column index: " + columnIndex); // This case will never happen
    }
    return Arrays.stream(UploadedMembershipRegistrationColumns.values())
        .filter(col -> col.getColumnIndex() == columnIndex)
        .map(UploadedMembershipRegistrationColumns::getColumnName)
        .findFirst()
        .get();
  }
}
