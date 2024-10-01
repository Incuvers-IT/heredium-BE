package art.heredium.payment.inicis.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.inf.PaymentTicketResponse;

@Getter
@Setter
public class InicisPayResponse implements PaymentTicketResponse {

  private String resultCode; // 결과코드 "0000":성공, 이외 실패 (실패코드 6byte)
  private String resultMsg; // 결과메세지
  private String tid; // 거래번호
  private String mid; // 상점아이디
  private String MOID; // 주문번호 결제 요청시 oid 필드에 설정된 값
  private String TotPrice; // 결제금액
  private String goodName; // 상품명
  private String payMethod; // 지불수단
  private String applDate; // 승인일자 [YYYYMMDD]
  private String applTime; // 승인시간 [hh24miss]
  private String EventCode; // 이벤트 코드, 카드 할부 및 행사 적용 코드
  private String buyerName; // 구매자명
  private String buyerTel; // 구매자 휴대폰번호
  private String buyerEmail; // 구매자 이메일주소
  private String custEmail; // 최종 이메일주소
  private String authSignature;
  private String timestamp;

  // 신용카드
  private String applNum; // 승인번호
  private String CARD_Num; // 신용카드번호
  private String CARD_Interest; // 상점부담 무이자 할부여부 ["1":상점부담 무이자]
  private String CARD_Quota; // 카드 할부기간
  private String CARD_Code; // 카드사 코드
  private String CARD_CorpFlag; // 카드구분 ["0":개인카드, "1":법인카드, "9":구분불가]
  private String CARD_CheckFlag; // 카드종류 ["0":신용카드, "1":체크카드, "2":기프트카드]
  private String CARD_PRTC_CODE; // 부분취소 가능여부 ["1":가능 , "0":불가능]
  private String CARD_BankCode; // 카드발급사(은행) 코드
  private String CARD_SrcCode; // 간편(앱)결제구분
  private String CARD_Point; // 카드포인트 사용여부 ["":카드 포인트 사용안함, "1":카드 포인트 사용]
  private String CARD_UsePoint; // 포인트 사용금액
  private String CARD_CouponPrice; // 실제 카드승인 금액
  private String CARD_CouponDiscount; // 쿠폰(즉시할인) 금액
  private String NAVERPOINT_UseFreePoint; // 네이버포인트 무상포인트
  private String NAVERPOINT_CSHRApplYN; // 네이버포인트 현금영수증 발행여부 ["Y":발행, "N":미발행]
  private String NAVERPOINT_CSHRApplAmt; // 네이버포인트 현금영수증 발행 금액
  private String PCO_OrderNo; // 페이코 주문번호
  private String currency; // 통화코드
  private String OrgPrice; // 달러 환전금액 해외카드 + 달러(USD) 결제 일 경우 환전금액

  // 계좌이체/가상계좌/휴대폰
  private String ACCT_BankCode; // 은행코드
  private String CSHR_ResultCode; // 현금영수증 발행 정상여부 ["220000": 정상]
  private String CSHR_Type; // 현금영수증구분 ["0":소득공제, "1":지출증빙]
  private String ACCT_Name; // 계좌주명
  private String VACT_Num; // 가상계좌번호
  private String VACT_BankCode; // 입금은행코드
  private String vactBankName; // 입금은행명
  private String VACT_Name; // 예금주명
  private String VACT_InputName; // 송금자명
  private String VACT_Date; // 입금기한일자 [YYYYMMDD]
  private String VACT_Time; // 입금기한시각 [hhmmss]
  private String HPP_Num; // 휴대폰번호
  private String payDevice; // 결제장치

  // 그외
  private String PayOption; // 포인트 사용구분
  private String OCB_Num; // OK CashBag 카드번호
  private String OCB_PayPrice; // OK CashBag 지불금액
  private String OCB_SaveApplNum; // OK CASHBAG 적립 승인번호
  private String OCB_PayApplNum; // OK CASHBAG 사용내역 승인번호
  private String OCB_ApplDate; // OK CASHBAG 승인일시
  private String UPoint_Num; // U포인트 카드번호
  private String UPoint_usablePoint; // U포인트 가용포인트
  private String UPoint_ApplPrice; // U포인트 승인요청 금액
  private String UPNT_PayOption; // U포인트 지불구분
  private String UPNT_SavePrice; // U포인트 복합결제시 여신 승인금액
  private String UPNT_PayPrice; // U포인트 지불금액
  private String GSPT_ApplPrice; // GS포인트 승인금액
  private String GSPT_SavePrice; // GS포인트 적립금액
  private String GSPT_PayPrice; // GS포인트 지불금액
  private String CULT_UserID; // 컬쳐랜트 고객ID
  private String GAMG_Cnt; // 스마트문화상품권 사용한 카드개수
  private String GAMG_ApplPrice; // 스마트문화상품권 승인금액
  private String GAMG_Num1; // 스마트문화상품권 카드번호
  private String GAMG_Price1; // 스마트문화상품권 카드결제금액
  private String TEEN_ApplPrice; // 틴캐시 승인금액
  private String TEEN_UserID; // 틴개시 사용자아이디
  private String TEEN_ApplNum; // 틴개시 승인번호
  private String BCSH_ApplPrice; // 도서상품권 승인금액
  private String BCSH_UserID; // 도서상품권 사용자아이디
  private String BCSH_ApplNum; // 도서상품권 승인번호
  private String PHNB_Num; // 승인 전화번호

  @Override
  public Long getAmount() {
    return Long.valueOf(TotPrice);
  }

  @Override
  public String getPaymentKey() {
    return tid;
  }

  @Override
  public String getPayMethod() {
    return payMethod;
  }
}
