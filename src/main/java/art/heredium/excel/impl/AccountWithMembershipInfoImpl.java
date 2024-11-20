package art.heredium.excel.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import art.heredium.domain.account.model.dto.response.AccountWithMembershipInfoResponse;
import art.heredium.excel.interfaces.CreateBody;

public class AccountWithMembershipInfoImpl implements CreateBody {

  private final List<AccountWithMembershipInfoResponse> list;

  public AccountWithMembershipInfoImpl(List<AccountWithMembershipInfoResponse> list) {
    this.list = list;
  }

  @Override
  public List<String> head() {
    List<String> headList = Arrays.asList("NO", "멤버십", "계정", "이름", "연락처", "최근로그인", "입장횟수", "생성일시");
    return new ArrayList<>(headList);
  }

  @Override
  public List<List<String>> body() {
    List<List<String>> bodyList = new ArrayList<>();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    int cnt = 0;
    for (AccountWithMembershipInfoResponse entity : list) {
      List<String> row =
          Arrays.asList(
              createString(list.size() - (cnt++)),
              createString(entity.getMembershipName()),
              createString(entity.getEmail()),
              createString(entity.getName()),
              createString(entity.getPhone()),
              createString(
                  entity.getLastLoginDate() != null ? entity.getLastLoginDate().format(dtf) : null),
              createString(entity.getNumberOfEntries()),
              createString(
                  entity.getCreatedDate() != null ? entity.getCreatedDate().format(dtf) : null));
      bodyList.add(row);
    }
    return bodyList;
  }

  private String createString(Object value) {
    return ObjectUtils.defaultIfNull(value, "").toString();
  }
}
