package art.heredium.excel.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import art.heredium.domain.account.model.dto.response.GetAdminAccountResponse;
import art.heredium.excel.interfaces.CreateBody;

public class AccountImpl implements CreateBody {

  private List<GetAdminAccountResponse> list;

  public AccountImpl(List<GetAdminAccountResponse> list) {
    this.list = list;
  }

  @Override
  public List<String> head() {
    List<String> headList =
        Arrays.asList(
            "NO", "아이디", "이름", "연락처", "가입일시", "최근 로그인일시", "마케팅 수신 동의","직업", "지역", "전시관람횟수");
    return new ArrayList<>(headList);
  }

  @Override
  public List<List<String>> body() {
    List<List<String>> bodyList1 = new ArrayList<>();
    DateTimeFormatter dtfd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter dtfs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    int cnt = 0;
    for (GetAdminAccountResponse entity : list) {
      String jobLabel = toJobLabel(entity.getJob());                       // ✅ 직업코드 → 라벨
      String region = toRegion(entity.getState(), entity.getDistrict());   // ✅ 지역 문자열

      List<String> asList =
          Arrays.asList(
              createString(list.size() - (cnt++)),
              createString(entity.getEmail()),
              createString(entity.getName()),
              createString(entity.getPhone()),
              createString(entity.getCreatedDate().format(dtfs)),
              createString(
                  entity.getLastLoginDate() != null
                      ? entity.getLastLoginDate().format(dtfs)
                      : null),
              createString(entity.getIsMarketingReceive() ? "동의" : "미동의"),
              createString(jobLabel),
              createString(region),
              createString(entity.getVisitCount()));
      bodyList1.add(asList);
    }
    return bodyList1;
  }

  private String createString(Object value) {
    return ObjectUtils.defaultIfNull(value, "").toString();
  }

  /** 직업코드 → 라벨 매핑 */
  private String toJobLabel(String code) {
    if (code == null) return "-";
    switch (code) {
      case "1":  return "학생";
      case "2":  return "취업준비생";
      case "3":  return "회사원";
      case "4":  return "공무원";
      case "5":  return "교사/교수";
      case "6":  return "자영업/프리랜서";
      case "7":  return "전문직";
      case "8":  return "서비스직";
      case "9":  return "생산직/기술직";
      case "10": return "금융업 종사자";
      case "11": return "IT 개발자";
      case "12": return "연구원";
      case "13": return "엔지니어";
      case "14": return "예술/문화/디자인";
      case "15": return "방송/연예/미디어";
      case "16": return "농림어업";
      case "17": return "가사/육아";
      case "18": return "무직";
      case "19": return "은퇴/퇴직자";
      case "20": return "기타";
      default:   return code; // 예상 밖 값이면 코드 그대로
    }
  }

  /** 지역 문자열 생성 (둘 다 없으면 '-') */
  private String toRegion(String state, String district) {
    String s = state == null ? "" : state.trim();
    String d = district == null ? "" : district.trim();
    if (s.isEmpty() && d.isEmpty()) return "-";
    if (s.isEmpty()) return d;
    if (d.isEmpty()) return s;
    return s + " " + d; // 엑셀에선 줄바꿈 대신 공백 권장
  }
}
