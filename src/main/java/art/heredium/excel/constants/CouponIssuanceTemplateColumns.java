package art.heredium.excel.constants;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CouponIssuanceTemplateColumns {
  EMAIL(0, "계정"),
  NAME(1, "이름"),
  PHONE(2, "연락처");

  private final int columnIndex;
  private final String columnName;

  public static String getColumnNameByIndex(int columnIndex) {
    if (columnIndex < 0 || columnIndex > 2) {
      throw new IndexOutOfBoundsException("Invalid column index: " + columnIndex);
    }
    return Arrays.stream(CouponIssuanceTemplateColumns.values())
        .filter(col -> col.getColumnIndex() == columnIndex)
        .map(CouponIssuanceTemplateColumns::getColumnName)
        .findFirst()
        .get();
  }
}
