package art.heredium.excel.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import art.heredium.domain.account.model.dto.request.GetAdminAccountRequest;
import art.heredium.domain.account.model.dto.request.GetAdminSleeperRequest;
import art.heredium.domain.account.model.dto.response.GetAdminAccountResponse;
import art.heredium.domain.account.model.dto.response.GetAdminSleeperResponse;
import art.heredium.domain.account.repository.AccountRepositoryImpl;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.model.dto.request.GetAdminTicketRequest;
import art.heredium.domain.ticket.repository.TicketRepositoryImpl;
import art.heredium.excel.constants.ExcelConstant;
import art.heredium.excel.impl.AccountImpl;
import art.heredium.excel.impl.SlepperImpl;
import art.heredium.excel.impl.TicketImpl;
import art.heredium.excel.manager.ExcelModelManager;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExcelService {

  private final TicketRepositoryImpl ticketRepository;
  private final AccountRepositoryImpl accountRepository;

  public Map<String, Object> ticketDownload(GetAdminTicketRequest dto, String fileName) {
    List<Ticket> list = ticketRepository.search(dto);
    ExcelModelManager emm = new ExcelModelManager();
    // 1. 파일 이름 부터 지정
    emm.setFileName(fileName);
    // 2. 시트 추가
    TicketImpl experience = new TicketImpl(list);
    emm.addHead(experience.head(), "Sheet1");
    emm.addBody(experience.body());
    // 자동으로 SHEET지 계산 후 Return.
    return emm.getMap();
  }

  public Map<String, Object> accountDownload(GetAdminAccountRequest dto, String fileName) {
    List<GetAdminAccountResponse> list = accountRepository.search(dto);
    ExcelModelManager emm = new ExcelModelManager();
    // 1. 파일 이름 부터 지정
    emm.setFileName(fileName);
    // 2. 시트 추가
    AccountImpl experience = new AccountImpl(list);
    emm.addHead(experience.head(), "Sheet1");
    emm.addBody(experience.body());
    // 자동으로 SHEET지 계산 후 Return.
    return emm.getMap();
  }

  public Map<String, Object> sleeperDownload(GetAdminSleeperRequest dto, String fileName) {
    List<GetAdminSleeperResponse> list = accountRepository.search(dto);
    ExcelModelManager emm = new ExcelModelManager();
    // 1. 파일 이름 부터 지정
    emm.setFileName(fileName);
    // 2. 시트 추가
    SlepperImpl experience = new SlepperImpl(list);
    emm.addHead(experience.head(), "Sheet1");
    emm.addBody(experience.body());
    // 자동으로 SHEET지 계산 후 Return.
    return emm.getMap();
  }

  private boolean isEmpty(Object obj) {
    boolean isNull = true;
    for (Field field : obj.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      Object keyValue = null;
      try {
        keyValue = field.get(obj);
      } catch (IllegalAccessException e) {
        // e.printStackTrace();
      }
      if (keyValue != null) {
        isNull = false;
        break;
      }
    }
    return isNull;
  }

  private String getStringCellValue(FormulaEvaluator evaluator, Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) return null;
    String str;
    if (cell.getCellType() == CellType.FORMULA) {
      final CellValue cellValue = evaluator.evaluate(row.getCell(index));
      str = cellValue.formatAsString().trim();
    } else {
      DataFormatter formatter = new DataFormatter();
      str = formatter.formatCellValue(row.getCell(index));
    }
    if (StringUtils.isBlank(str)) return null;
    return str;
  }

  private String getStringCellValue(
      FormulaEvaluator evaluator, Row row, int index, String defaultValue) {
    Cell cell = row.getCell(index);
    if (cell == null) return defaultValue;
    String str;
    if (cell.getCellType() == CellType.FORMULA) {
      final CellValue cellValue = evaluator.evaluate(row.getCell(index));
      str = cellValue.formatAsString().trim();
    } else {
      DataFormatter formatter = new DataFormatter();
      str = formatter.formatCellValue(row.getCell(index));
    }
    if (StringUtils.isBlank(str)) return defaultValue;
    return str;
  }

  private Integer getNumericCellValue(FormulaEvaluator evaluator, Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) return null;
    String str;
    if (cell.getCellType() == CellType.FORMULA) {
      final CellValue cellValue = evaluator.evaluate(row.getCell(index));
      str = cellValue.formatAsString().trim();
    } else {
      DataFormatter formatter = new DataFormatter();
      str = formatter.formatCellValue(row.getCell(index)).trim();
    }
    str = str.replace("\u00a0", "");
    if (StringUtils.isBlank(str)) return null;
    return (int) Double.parseDouble(str.replaceAll(",", ""));
  }

  private Integer getNumericCellValue(
      FormulaEvaluator evaluator, Row row, int index, Integer defaultValue) {
    Cell cell = row.getCell(index);
    if (cell == null) return defaultValue;
    String str;
    if (cell.getCellType() == CellType.FORMULA) {
      final CellValue cellValue = evaluator.evaluate(row.getCell(index));
      str = cellValue.formatAsString().trim();
    } else {
      DataFormatter formatter = new DataFormatter();
      str = formatter.formatCellValue(row.getCell(index)).trim();
    }
    str = str.replace("\u00a0", "");
    if (StringUtils.isBlank(str)
        || str.equals("\"\"")
        || str.equals("#REF!")
        || str.equals("-")
        || str.equals("별도상담")) return defaultValue;
    return (int) Double.parseDouble(str.replaceAll(",", ""));
  }

  private Workbook readWorkbook(MultipartFile multipartFile) {
    try {
      ZipSecureFile.setMinInflateRatio(0);
      if (isExcelXlsx(multipartFile.getOriginalFilename())) {
        return new XSSFWorkbook(multipartFile.getInputStream());
      }
      if (isExcelXls(multipartFile.getOriginalFilename())) {
        return new HSSFWorkbook(multipartFile.getInputStream());
      }
    } catch (IOException e) {
      // log.error("readWorkbook", e);
    }
    return null;
  }

  private boolean isExcelExtension(String fileName) {
    return fileName.endsWith(ExcelConstant.XLS) || fileName.endsWith(ExcelConstant.XLSX);
  }

  private boolean isExcelXls(String fileName) {
    return fileName.endsWith(ExcelConstant.XLS);
  }

  private boolean isExcelXlsx(String fileName) {
    return fileName.endsWith(ExcelConstant.XLSX);
  }
}
