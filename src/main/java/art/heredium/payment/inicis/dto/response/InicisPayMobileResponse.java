package art.heredium.payment.inicis.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.inf.PaymentTicketResponse;

@Getter
@Setter
public class InicisPayMobileResponse implements PaymentTicketResponse {
  private String P_STATUS; // 결과코드 "00":성공, 이외 실패 (실패코드 4byte)
  private String P_RMESG1; // 결과메세지
  private String P_TID; // 거래번호
  private String P_MID; // 상점아이디
  private String P_OID; // 주문번호
  private String P_AMT; // 거래금액
  private String P_TYPE; // 지불수단
  private String P_AUTH_DT; // 승인일자 [YYYYMMDDhhmmss]
  private String P_UNAME; // 구매자명
  private String P_MNAME; // 가맹점명    결제요청 정보에 입력된 값 반환
  private String P_NOTI; // 가맹점 임의 데이터
  private String P_NOTEURL; // 가맹점 전달 P_NOTI_URL
  private String P_NEXT_URL; // 가맹점 전달 P_NEXT_URL

  // 신용카드
  private String P_AUTH_NO; // 승인번호
  private String P_CARD_NUM; // 신용카드번호
  private String P_CARD_INTEREST; // 상점부담 무이자 할부여부 ["1":상점부담 무이자]
  private String P_RMESG2; // 카드 할부기간
  private String P_FN_CD1; // 카드사 코드
  private String P_FN_NM; // 결제카드사 한글명
  private String CARD_CorpFlag; // 카드구분 ["0":개인카드, "1":법인카드, "9":구분불가]
  private String P_CARD_CHECKFLAG; // 카드종류 ["0":신용카드, "1":체크카드, "2":기프트카드]
  private String P_CARD_PRTC_CODE; // 부분취소 가능여부 ["1":가능 , "0":불가능]
  private String P_CARD_ISSUER_CODE; // 카드발급사(은행) 코드
  private String P_ISP_CARDCODE; // VP 카드코드
  private String P_SRC_CODE; // 간편(앱)결제구분
  private String P_CARD_MEMBER_NUM; // 가맹점번호    자체가맹점인 경우만
  private String P_CARD_PURCHASE_CODE; // 매입사코드    자체가맹점인 경우만
  private String P_CARD_USEPOINT; // 포인트 사용금액
  private String P_COUPONFLAG; // 쿠폰사용 유무 ["1":사용]
  private String P_COUPON_DISCOUNT; // 쿠폰(즉시할인) 금액
  private String P_CARD_APPLPRICE; // 승인요청 금액
  private String P_CARD_COUPON_PRICE; // 실제 카드승인 금액
  private String NAVERPOINT_UseFreePoint; // 네이버포인트 무상포인트
  private String NAVERPOINT_CSHRApplYN; // 네이버포인트 현금영수증 발행여부 ["Y":발행, "N":미발행]
  private String NAVERPOINT_CSHRApplAmt; // 네이버포인트 현금영수증 발행 금액
  private String PCO_OrderNo; // 페이코 주문번호
  private String CARD_EmpPrtnCode; // 롯데카드 임직원 제휴 구분코드 ["L":임직원]    롯데카드인 경우만 임직원 구분코드 전달
  private String CARD_NomlMobPrtnCode; // 카드사 제휴구분코드 ["P":롯데카드일반, "M":롯데카드모바일, "H":현대카드(통합)]

  // 계좌이체/가상계좌/휴대폰
  // private String P_FN_CD1;	//은행코드
  // private String P_FN_NM;	//결제은행 한글명
  private String P_VACT_NUM; // 가상계좌번호
  private String P_VACT_BANK_CODE; // 입금은행코드
  // private String P_FN_NM;	//입금은행명
  private String P_VACT_NAME; // 예금주명
  private String P_VACT_DATE; // 입금기한일자 [YYYYMMDD]
  private String P_VACT_TIME; // 입금기한시각 [hhmmss]
  private String P_HPP_NUM; // 휴대폰번호
  private String P_HPP_CORP; // 휴대폰통신사 [*** 고정]

  // 현금영수증
  private String P_CSHR_CODE; // 결과코드 ["0000":정상, 그외 실패]
  private String P_CSHR_MSG; // 결과메세지
  private String P_CSHR_AMT; // 현금영수증 총 금액 [총금액 = 공급가액+세금+봉사료]
  private String P_CSHR_SUP_AMT; // 공급가액
  private String P_CSHR_TAX; // 부가세
  private String P_CSHR_SRVC_AMT; // 봉사료
  private String P_CSHR_TYPE; // 용도구분 ["0":소득공제, "1":지출증빙]
  private String P_CSHR_DT; // 발행일시 [YYYYMMDDhhmmss]
  private String P_CSHR_AUTH_NO; // 발행 승인번호 가상계좌는 채번시점에선 미전달 (입금통보로 전달)

  @Override
  public Long getAmount() {
    return Long.valueOf(P_AMT);
  }

  @Override
  public String getPaymentKey() {
    return P_TID;
  }

  @Override
  public String getPayMethod() {
    return P_TYPE;
  }
}
