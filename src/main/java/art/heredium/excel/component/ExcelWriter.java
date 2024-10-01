package art.heredium.excel.component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import art.heredium.excel.constants.ExcelConstant;

@SuppressWarnings("unchecked")
public class ExcelWriter {

  private Workbook workbook;
  private Map<String, Object> model;
  private HttpServletRequest request;
  private HttpServletResponse response;

  public ExcelWriter(
      Workbook workbook,
      Map<String, Object> model,
      HttpServletRequest request,
      HttpServletResponse response) {
    this.workbook = workbook;
    this.model = model;
    this.request = request;
    this.response = response;
  }

  public void create() {
    applyFileNameForRequest(mapToFileName());

    applyContentTypeForRequest();

    int sheetSize = 1;
    try {
      sheetSize = Integer.parseInt(String.valueOf(model.get(ExcelConstant.SHEET_SIZE)));
    } catch (Exception e) {
      // e.printStackTrace();
    }

    for (int i = 0; i < sheetSize; i++) {
      Sheet sheet = workbook.createSheet();
      workbook.setSheetName(i, getSheetName(i));
      createHead(sheet, mapToHeadList(i));
      createBody(sheet, mapToBodyList(i));
    }
  }

  private String getSheetName(int i) {
    return (String) model.get(ExcelConstant.SHEET_NAME + i);
  }

  private String mapToFileName() {
    return (String) model.get(ExcelConstant.FILE_NAME);
  }

  private List<String> mapToHeadList(int i) {
    return (List<String>) model.get(ExcelConstant.HEAD + i);
  }

  private List<List<String>> mapToBodyList(int i) {
    return (List<List<String>>) model.get(ExcelConstant.BODY + i);
  }

  private void applyFileNameForRequest(String fileName) {
    if (fileName == null) fileName = "";
    try {
      response.setHeader(
          "Content-Disposition",
          "attachment; filename=\""
              + URLEncoder.encode(appendFileExtension(fileName), "UTF-8")
              + "\"");
    } catch (UnsupportedEncodingException e) {
      // e.printStackTrace();
    }
  }

  private String appendFileExtension(String fileName) {
    if (workbook instanceof XSSFWorkbook || workbook instanceof SXSSFWorkbook) {
      fileName += ".xlsx";
    }
    if (workbook instanceof HSSFWorkbook) {
      fileName += ".xls";
    }

    return fileName;
  }

  private void applyContentTypeForRequest() {
    if (workbook instanceof XSSFWorkbook || workbook instanceof SXSSFWorkbook) {
      response.setHeader(
          "Content-Type",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8");
    }
    if (workbook instanceof HSSFWorkbook) {
      response.setHeader("Content-Type", "application/vnd.ms-excel; charset=UTF-8");
    }
  }

  private void createHead(Sheet sheet, List<String> headList) {
    createRow(sheet, headList, 0);
  }

  private void createBody(Sheet sheet, List<List<String>> bodyList) {
    int rowSize = bodyList.size();
    for (int i = 0; i < rowSize; i++) {
      createRow(sheet, bodyList.get(i), i + 1);
    }
  }

  private void createRow(Sheet sheet, List<String> cellList, int rowNum) {
    int size = cellList.size();
    Row row = sheet.createRow(rowNum);

    for (int i = 0; i < size; i++) {
      row.createCell(i).setCellValue(cellList.get(i));
    }
  }
}
