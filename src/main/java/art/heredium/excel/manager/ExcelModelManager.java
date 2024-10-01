package art.heredium.excel.manager;

import java.util.HashMap;
import java.util.Map;

import art.heredium.excel.constants.ExcelConstant;

public class ExcelModelManager {

  private int headCnt = 0;
  private int bodyCnt = 0;
  private Map<String, Object> map = new HashMap<>();

  public void addHead(Object o, String sheetName) {
    map.put(String.format("%s%d", ExcelConstant.SHEET_NAME, headCnt), sheetName);
    map.put(String.format("%s%d", ExcelConstant.HEAD, headCnt), o);
    headCnt++;
  }

  public void addBody(Object o) {
    map.put(String.format("%s%d", ExcelConstant.BODY, bodyCnt++), o);
  }

  public void setFileName(String fileName) {
    map.put(ExcelConstant.FILE_NAME, fileName);
  }

  public Map<String, Object> getMap() {
    map.put(ExcelConstant.SHEET_SIZE, Math.max(headCnt, bodyCnt));
    return map;
  }
}
