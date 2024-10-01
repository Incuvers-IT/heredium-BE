package art.heredium.hanabank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HanaParamsResponse {
  // { "grade": "normal", "custNo": "123456789", "emalAdr": "jang@spadecompany.kr", "mbphNo":
  // "01029354191", "btdy": "19940812", "custNm": "장원준" }
  private String grade;
  private String custNo;
  private String emalAdr;
  private String mbphNo;
  private String btdy;
  private String custNm;
}
