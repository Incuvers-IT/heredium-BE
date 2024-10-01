package art.heredium.payment.inicis.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.payment.inf.PaymentTicketRequest;

@Getter
@Setter
@ToString
public class InicisTicketRequest implements PaymentTicketRequest {
  private String resultCode;
  private String resultMsg;
  private String mid;
  private String orderNumber;
  private String authToken;
  private String authUrl;
  private String netCancelUrl;
  private String checkAckUrl;
  private String charset;
  private String merchantData;

  // 모바일 request
  private Boolean isMobile;

  @JsonProperty("P_STATUS")
  private String P_STATUS; // 결과코드 "00":성공, 이외 실패

  @JsonProperty("P_RMESG1")
  private String P_RMESG1; // 결과메시지

  @JsonProperty("P_TID")
  private String P_TID; // 인증거래번호, 성공시에만 전달

  @JsonProperty("P_AMT")
  private String P_AMT; // 거래금액

  @JsonProperty("P_REQ_URL")
  private String P_REQ_URL; // 승인요청 URL, 해당 URL로 HTTPS API Request 승인요청 - POST

  @JsonProperty("P_NOTI")
  private String P_NOTI; // 가맹점 임의 데이터

  @Override
  public String getOrderId() {
    if (isMobile) {
      return P_NOTI;
    } else {
      return orderNumber;
    }
  }
}
