package art.heredium.excel.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import art.heredium.domain.membership.model.dto.response.ActiveMembershipRegistrationsResponse;
import art.heredium.excel.interfaces.CreateBody;

public class ActiveMembershipImpl implements CreateBody {

  private List<ActiveMembershipRegistrationsResponse> list;

  public ActiveMembershipImpl(List<ActiveMembershipRegistrationsResponse> list) {
    this.list = list;
  }

  @Override
  public List<String> head() {
    List<String> headList =
        Arrays.asList(
            "NO",
            "멤버십 이름",
            "계정", // account (value is user's email)
            "이름",
            "연락처",
//            "결제상태",
//            "결제일시",
//            "멤버십횟수",
            "전시사용횟수",
            "프로그램사용횟수",
            "커피사용횟수",
            "마일리지내역"
//            "마케팅수신동의",
        );
    return new ArrayList<>(headList);
  }

  @Override
  public List<List<String>> body() {
    List<List<String>> bodyList1 = new ArrayList<>();
    DateTimeFormatter dtfd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    int cnt = 0;
    for (ActiveMembershipRegistrationsResponse entity : list) {
      List<String> asList =
          Arrays.asList(
              createString(list.size() - (cnt++)),
              createString(entity.getMembershipOrCompanyName()),
              createString(entity.getEmail()),
              createString(entity.getName()),
              createString(entity.getPhone()),
//              createString(entity.getPaymentStatus()),
//              createString(entity.getPaymentDate().format(dtfd)),
//              createString(entity.getNumberOfMemberships()),
              createString(entity.getNumberOfExhibitionsUsed()),
              createString(entity.getNumberOfProgramsUsed()),
              createString(entity.getNumberOfCoffeeUsed()),
              createString(entity.getMileageSum())
//              createString(entity.getIsAgreeToReceiveMarketing())
          );
      bodyList1.add(asList);
    }
    return bodyList1;
  }

  private String createString(Object value) {
    return ObjectUtils.defaultIfNull(value, "").toString();
  }
}
