package art.heredium.excel.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import art.heredium.domain.account.model.dto.response.AccountWithMembershipInfoIncludingTitleResponse;
import art.heredium.excel.interfaces.CreateBody;

public class AccountInfoImpl implements CreateBody {

  private List<AccountWithMembershipInfoIncludingTitleResponse> list;

  public AccountInfoImpl(List<AccountWithMembershipInfoIncludingTitleResponse> list) {
    this.list = list;
  }

  @Override
  public List<String> head() {
    List<String> headList =
        Arrays.asList(
            "NO", "멤버십 이름", "제목", "지불 상태", "지불 날짜", "시작 날짜", "종 료일", "쿠폰 수", "이메일", "이름", "전화 번호");
    return new ArrayList<>(headList);
  }

  @Override
  public List<List<String>> body() {
    List<List<String>> bodyList1 = new ArrayList<>();
    DateTimeFormatter dtfd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    int cnt = 0;
    for (AccountWithMembershipInfoIncludingTitleResponse entity : list) {
      List<String> asList =
          Arrays.asList(
              createString(list.size() - (cnt++)),
              createString(entity.getMembershipName()),
              createString(entity.getTitle()),
              createString(entity.getPaymentStatus() != null ? entity.getPaymentStatus().getDesc() : null),
              createString(
                  entity.getPaymentDate() != null ? entity.getPaymentDate().format(dtfd) : null),
              createString(
                  entity.getStartDate() != null ? entity.getStartDate().format(dtfd) : null),
              createString(entity.getEndDate() != null ? entity.getEndDate().format(dtfd) : null),
              createString(entity.getNumberOfUsedCoupons()),
              createString(entity.getEmail()),
              createString(entity.getName()),
              createString(entity.getPhone()));
      bodyList1.add(asList);
    }
    return bodyList1;
  }

  private String createString(Object value) {
    return ObjectUtils.defaultIfNull(value, "").toString();
  }
}
