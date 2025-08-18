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
            "멤버십",
            "상태",
            "시작일시",
            "종료일시",
            "전시 쿠폰 사용",
            "프로그램 쿠폰 사용",
            "커피 쿠폰 사용",
//            "전시사용횟수",
//            "프로그램사용횟수",
//            "커피사용횟수",
            "이용금액",
            "계정",
            "이름",
            "연락처"
//            "생성 날짜",
//            "마지막 로그인 날짜",
//            "멤버십횟수",
//            "전시사용횟수",
//            "프로그램사용횟수",
//            "마케팅 동의"
        );
    return new ArrayList<>(headList);
  }

  @Override
  public List<List<String>> body() {
    List<List<String>> bodyList1 = new ArrayList<>();
    DateTimeFormatter dtfd = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    DateTimeFormatter dtfdWithTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    int cnt = 0;
    for (AccountWithMembershipInfoExcelDownloadResponse entity : list) {
      List<String> asList =
          Arrays.asList(
              createString(list.size() - (cnt++)),
              createString(entity.getMembershipName()),
              createString(entity.getPaymentStatus()),
//              createString(
//                  entity.getPaymentDate() != null ? entity.getPaymentDate().format(dtfd) : null),
              createString(
                  entity.getStartDate() != null ? entity.getStartDate().format(dtfd) : null),
              createString(entity.getEndDate() != null ? entity.getEndDate().format(dtfd) : null),
              createString(entity.getNumberOfUsedExhibitionCoupons()),
              createString(entity.getNumberOfUsedProgramCoupons()),
              createString(entity.getNumberOfUsedCoffeeCoupons()),
//              createString(entity.getNumberOfExhibitionTickets()),
//              createString(entity.getNumberOfProgramTickets()),
//              createString(entity.getNumberOfCoffeeTickets()),
              createString(entity.getAmount()),
              createString(entity.getEmail()),
              createString(entity.getName()),
              createString(entity.getPhone())
//              createString(
//                  entity.getCreatedDate() != null ? entity.getCreatedDate().format(dtfd) : null),
//              createString(
//                  entity.getLastLoginDate() != null
//                      ? entity.getLastLoginDate().format(dtfdWithTime)
//                      : null),
//              createString(entity.getNumberOfActiveMemberships())
//              createString(entity.getMarketingConsent())
          );
      bodyList1.add(asList);
    }
    return bodyList1;
  }

  private String createString(Object value) {
    return ObjectUtils.defaultIfNull(value, "").toString();
  }
}
