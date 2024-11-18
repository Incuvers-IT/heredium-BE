package art.heredium.excel.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import art.heredium.domain.account.model.dto.response.AccountWithMembershipInfoExcelDownloadResponse;
import art.heredium.excel.interfaces.CreateBody;

public class AccountInfoImpl implements CreateBody {

  private List<AccountWithMembershipInfoExcelDownloadResponse> list;

  public AccountInfoImpl(List<AccountWithMembershipInfoExcelDownloadResponse> list) {
    this.list = list;
  }

  @Override
  public List<String> head() {
    List<String> headList =
        Arrays.asList(
            "NO",
            "멤버십 이름",
            "제목",
            "지불 상태",
            "지불 날짜",
            "시작 날짜",
            "종 료일",
            "쿠폰 수",
            "이메일",
            "이름",
            "전화 번호",
            "금액",
            "생성 날짜",
            "마지막 로그인 날짜",
            "사용 횟수",
            "마케팅 동의");
    return new ArrayList<>(headList);
  }

  @Override
  public List<List<String>> body() {
    List<List<String>> bodyList1 = new ArrayList<>();
    DateTimeFormatter dtfd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    int cnt = 0;
    for (AccountWithMembershipInfoExcelDownloadResponse entity : list) {
      List<String> asList =
          Arrays.asList(
              createString(list.size() - (cnt++)),
              createString(entity.getMembershipName()),
              createString(entity.getTitle()),
              createString(entity.getPaymentStatus()),
              createString(
                  entity.getPaymentDate() != null ? entity.getPaymentDate().format(dtfd) : null),
              createString(
                  entity.getStartDate() != null ? entity.getStartDate().format(dtfd) : null),
              createString(entity.getEndDate() != null ? entity.getEndDate().format(dtfd) : null),
              createString(entity.getNumberOfUsedCoupons()),
              createString(entity.getEmail()),
              createString(entity.getName()),
              createString(entity.getPhone()),
              createString(entity.getAmount()),
              createString(
                  entity.getCreatedDate() != null ? entity.getCreatedDate().format(dtfd) : null),
              createString(
                  entity.getLastLoginDate() != null
                      ? entity.getLastLoginDate().format(dtfd)
                      : null),
              createString(entity.getUsageCount()),
              createString(entity.getMarketingConsent()));
      bodyList1.add(asList);
    }
    return bodyList1;
  }

  private String createString(Object value) {
    return ObjectUtils.defaultIfNull(value, "").toString();
  }
}
