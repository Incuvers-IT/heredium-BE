package art.heredium.excel.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import art.heredium.domain.account.model.dto.response.GetAdminSleeperResponse;
import art.heredium.excel.interfaces.CreateBody;

public class SlepperImpl implements CreateBody {

  private List<GetAdminSleeperResponse> list;

  public SlepperImpl(List<GetAdminSleeperResponse> list) {
    this.list = list;
  }

  @Override
  public List<String> head() {
    List<String> headList =
        Arrays.asList("NO", "아이디", "이름", "연락처", "가입일시", "최근 로그인일시", "마케팅 수신 동의", "전시관람횟수");
    return new ArrayList<>(headList);
  }

  @Override
  public List<List<String>> body() {
    List<List<String>> bodyList1 = new ArrayList<>();
    DateTimeFormatter dtfs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    int cnt = 0;
    for (GetAdminSleeperResponse entity : list) {
      List<String> asList =
          Arrays.asList(
              createString(list.size() - (cnt++)),
              createString(entity.getEmail()),
              createString(entity.getName()),
              createString(entity.getPhone()),
              createString(entity.getCreatedDate().format(dtfs)),
              createString(entity.getSleepDate().format(dtfs)),
              createString(entity.getIsMarketingReceive() ? "동의" : "미동의"),
              createString(entity.getVisitCount()));
      bodyList1.add(asList);
    }
    return bodyList1;
  }

  private String createString(Object value) {
    return ObjectUtils.defaultIfNull(value, "").toString();
  }
}
