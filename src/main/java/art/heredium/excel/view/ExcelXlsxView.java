package art.heredium.excel.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import org.apache.poi.ss.usermodel.Workbook;

import art.heredium.excel.component.ExcelWriter;

@Component("xlsxView")
public class ExcelXlsxView extends AbstractXlsxView {

  @Override
  protected void buildExcelDocument(
      Map<String, Object> model,
      Workbook workbook,
      HttpServletRequest request,
      HttpServletResponse response) {
    new ExcelWriter(workbook, model, request, response).create();
  }
}
