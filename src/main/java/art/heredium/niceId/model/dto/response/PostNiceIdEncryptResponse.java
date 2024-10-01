package art.heredium.niceId.model.dto.response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostNiceIdEncryptResponse {
  private String requestNumber; // 요청 번호
  //    private String responseNumber;        // 인증 고유번호
  //    private String authType;                // 인증 수단
  private String name; // 성명
  //    private String name_UTF8;                //charset utf8 사용시 이름
  //    private String dupInfo;                // 중복가입 확인값 (DI_64 byte)
  //    private String connInfo;                // 연계정보 확인값 (CI_88 byte)
  private LocalDate birthDate; // 생년월일(YYYYMMDD)
  //    private GenderType gender;                // 성별
  //    private String nationalInfo;            // 내/외국인정보 (개발가이드 참조)
  private String mobileNo; // 휴대폰번호
  //    private String mobileCo;                // 통신사

  public PostNiceIdEncryptResponse(HashMap map) {
    this.requestNumber = (String) map.get("REQ_SEQ");
    //        this.responseNumber = (String) map.get("RES_SEQ");
    //        this.authType = (String) map.get("AUTH_TYPE");
    this.name = (String) map.get("NAME");
    //        this.name_UTF8 = (String) map.get("UTF8_NAME");
    this.birthDate =
        LocalDate.parse((String) map.get("BIRTHDATE"), DateTimeFormatter.ofPattern("yyyyMMdd"));
    //        this.gender = map.get("GENDER").equals("0") ? GenderType.WOMAN : GenderType.MAN;
    //        this.nationalInfo = (String) map.get("NATIONALINFO");
    //        this.dupInfo = (String) map.get("DI");
    //        this.connInfo = (String) map.get("CI");
    this.mobileNo = (String) map.get("MOBILE_NO");
    //        this.mobileCo = (String) map.get("MOBILE_CO");
  }
}
