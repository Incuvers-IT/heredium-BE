package art.heredium.payment.inicis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.inicis.std.util.HttpUtil;
import com.inicis.std.util.SignatureUtil;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.inf.PaymentService;
import art.heredium.payment.inf.PaymentTicketResponse;
import art.heredium.payment.inicis.dto.request.InicisTicketRequest;
import art.heredium.payment.inicis.dto.response.InicisPayMobileResponse;
import art.heredium.payment.inicis.dto.response.InicisPayResponse;
import art.heredium.payment.inicis.dto.response.InicisValidResponse;
import art.heredium.payment.type.PaymentType;

@Slf4j
@Service
public class Inicis implements PaymentService<InicisValidResponse, InicisTicketRequest> {

  @Value("${inicis.mid}")
  private String MID;

  @Value("${inicis.sign-key}")
  private String SIGN_KEY;

  @Value("${inicis.api-key}")
  private String API_KEY;

  @Override
  public PaymentType getPaymentType(InicisTicketRequest dto) {
    return PaymentType.INICIS;
  }

  @Override
  public InicisValidResponse valid(Ticket ticket) {

    try {
      String mid = MID; // 가맹점 ID(가맹점 수정후 고정)
      String signKey = SIGN_KEY; // 가맹점에 제공된 웹 표준 사인키(가맹점 수정후 고정)
      String timestamp = SignatureUtil.getTimestamp(); // util에 의해서 자동생성

      String oid = ticket.getUuid(); // 가맹점 주문번호(가맹점에서 직접 설정)
      Long price = ticket.getPrice(); // 상품가격(특수기호 제외, 가맹점에서 직접 설정)
      String mKey = SignatureUtil.hash(signKey, "SHA-256");

      Map<String, String> signParam = new HashMap<>();
      signParam.put("oid", oid);
      signParam.put("price", price.toString());
      signParam.put("timestamp", timestamp);
      String signature = SignatureUtil.makeSignature(signParam);

      return new InicisValidResponse(ticket, timestamp, oid, price, mid, mKey, signature);
    } catch (Exception e) {
      throw new ApiException(ErrorCode.BAD_REQUEST);
    }
  }

  @Override
  public PaymentTicketResponse pay(InicisTicketRequest dto, Long amount) {
    if (dto.getIsMobile()) {
      return payMobile(dto);
    }

    try {
      // #####################
      // 인증이 성공일 경우만
      // #####################
      if ("0000".equals(dto.getResultCode())) {

        // log.info("####인증성공/승인요청####");

        // ############################################
        // 1.전문 필드 값 설정(***가맹점 개발수정***)
        // ############################################

        String charset = "UTF-8"; // 리턴형식[UTF-8,EUC-KR](가맹점 수정후 고정)
        String format = "JSON"; // 리턴형식[XML,JSON,NVP](가맹점 수정후 고정)
        String timestamp = SignatureUtil.getTimestamp(); // util에 의해서 자동생성
        String mid = dto.getMid(); // 가맹점 ID 수신 받은 데이터로 설정
        String authToken = dto.getAuthToken(); // 취소 요청 tid에 따라서 유동적(가맹점 수정후 고정)
        String authUrl = dto.getAuthUrl(); // 승인요청 API url(수신 받은 값으로 설정, 임의 세팅 금지)
        String netCancel = dto.getNetCancelUrl(); // 망취소 API url(수신 받은 값으로 설정, 임의 세팅 금지)
        String merchantData = dto.getMerchantData(); // 가맹점 관리데이터 수신

        // #####################
        // 2.signature 생성
        // #####################
        Map<String, String> signParam = new HashMap<>();
        signParam.put("authToken", authToken); // 필수
        signParam.put("timestamp", timestamp); // 필수

        // signature 데이터 생성 (모듈에서 자동으로 signParam을 알파벳 순으로 정렬후 NVP 방식으로 나열해 hash)
        String signature = SignatureUtil.makeSignature(signParam);

        // 1. 가맹점에서 승인시 주문번호가 변경될 경우 (선택입력) 하위 연결.
        // String oid = "";

        // #####################
        // 3.API 요청 전문 생성
        // #####################
        Map<String, String> authMap = new Hashtable<>();
        authMap.put("mid", mid); // 필수
        authMap.put("authToken", authToken); // 필수
        authMap.put("signature", signature); // 필수
        authMap.put("timestamp", timestamp); // 필수
        authMap.put("charset", charset); // default=UTF-8
        authMap.put("format", format); // default=XML

        // log.info("##승인요청 API 요청##");

        HttpUtil httpUtil = new HttpUtil();

        try {
          // #####################
          // 4.API 통신 시작
          // #####################

          String authResultString = httpUtil.processHTTP(authMap, authUrl);

          // ############################################################
          // 5.API 통신결과 처리(***가맹점 개발수정***)
          // ############################################################
          // log.infoln("## 승인 API 결과 ##");

          InicisPayResponse response =
              new Gson().fromJson(authResultString, InicisPayResponse.class);

          // String test = authResultString.replace(",", "&").replace(":", "=").replace("\"",
          // "").replace(" ", "").replace("\n", "").replace("}", "").replace("{", "");

          //                    Map<String, String> resultMap = ParseUtil.parseStringToMap(test);
          // //문자열을 MAP형식으로 파싱
          // log.info("resultMap == " + resultMap);

          /*************************  결제보안 강화 2016-05-18 START ****************************/
          Map<String, String> secureMap = new HashMap<>();
          secureMap.put("mid", mid); // mid
          secureMap.put("tstamp", timestamp); // timestemp
          secureMap.put("MOID", response.getMOID()); // MOID
          secureMap.put("TotPrice", response.getTotPrice()); // TotPrice

          // signature 데이터 생성
          String secureSignature = SignatureUtil.makeSignatureAuth(secureMap);
          /*************************  결제보안 강화 2016-05-18 END ****************************/

          if ("0000".equals(response.getResultCode())
              && secureSignature.equals(response.getAuthSignature())) { // 결제보안 강화 2016-05-18
            /*****************************************************************************
             * 여기에 가맹점 내부 DB에 결제 결과를 반영하는 관련 프로그램 코드를 구현한다.
             *
             * [중요!] 승인내용에 이상이 없음을 확인한 뒤 가맹점 DB에 해당건이 정상처리 되었음을 반영함
             * 처리중 에러 발생시 망취소를 한다.
             ******************************************************************************/
            response.setTimestamp(timestamp);
            return response;
          } else {
            log.error("결과 코드 : " + response.getResultCode());
            log.error("결과 내용 : " + response.getResultMsg());

            // 결제보안키가 다른 경우
            if (!secureSignature.equals(response.getAuthSignature())
                && "0000".equals(response.getResultCode())) {
              // 결과정보
              log.error("데이터 위변조 체크 실패");

              // 망취소
              if ("0000".equals(response.getResultCode())) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "데이터 위변조 체크 실패");
              }
            }
          }

          // 수신결과를 파싱후 resultCode가 "0000"이면 승인성공 이외 실패
          // 가맹점에서 스스로 파싱후 내부 DB 처리 후 화면에 결과 표시

          // payViewType을 popup으로 해서 결제를 하셨을 경우
          // 내부처리후 스크립트를 이용해 opener의 화면 전환처리를 하세요

          throw new ApiException(ErrorCode.BAD_REQUEST, "결제 실패");
        } catch (Exception ex) {

          // ####################################
          // 실패시 처리(***가맹점 개발수정***)
          // ####################################

          // ---- db 저장 실패시 등 예외처리----//
          log.error("pay db", ex);

          // #####################
          // 망취소 API
          // #####################
          cancel(authMap, netCancel); // 망취소 요청 API url(고정, 임의 세팅 금지)

          // log.infoln("## 망취소 API 결과 ##");
          // 취소 결과 확인
          throw new ApiException(ErrorCode.BAD_REQUEST, "pay db 등록 실패");
        }
      } else {
        throw new ApiException(ErrorCode.BAD_REQUEST, "실패");
      }
    } catch (Exception e) {
      throw new ApiException(ErrorCode.BAD_REQUEST, "signature 생성 실패");
    }
  }

  @Override
  public void cancel(Ticket ticket, InicisTicketRequest dto) {
    try {
      String timestamp = SignatureUtil.getTimestamp();
      Map<String, String> signParam = new HashMap<>();
      signParam.put("authToken", dto.getAuthToken());
      signParam.put("timestamp", timestamp);
      String signature = SignatureUtil.makeSignature(signParam);

      Map<String, String> authMap = new Hashtable<>();
      authMap.put("mid", dto.getMid());
      authMap.put("authToken", dto.getAuthToken());
      authMap.put("signature", signature);
      authMap.put("timestamp", timestamp);
      authMap.put("charset", dto.getCharset());
      authMap.put("format", "JSON");

      cancel(authMap, dto.getNetCancelUrl());
    } catch (Exception e) {
      throw new ApiException(ErrorCode.BAD_REQUEST, "망 취소 실패");
    }
  }

  public void cancel(Map<String, String> authMap, String netCancel) {
    HttpUtil httpUtil = new HttpUtil();
    try {
      httpUtil.processHTTP(authMap, netCancel); // 망취소 요청 API url(고정, 임의 세팅 금지)
    } catch (Exception e) {
      throw new ApiException(ErrorCode.BAD_REQUEST, "망 취소 실패");
    }
  }

  public InicisPayMobileResponse payMobile(InicisTicketRequest dto) {
    try {
      String P_STATUS = dto.getP_STATUS(); // 인증 상태
      String P_RMESG1 = dto.getP_RMESG1(); // 인증 결과 메시지
      String P_TID = dto.getP_TID(); // 인증 거래번호
      String P_REQ_URL = dto.getP_REQ_URL(); // 결제요청 URL
      String P_NOTI = dto.getP_NOTI(); // 기타주문정보

      String P_MID = MID;

      if (P_STATUS.equals("00")) { // 인증결과가 실패일 경우

        // 승인요청할 데이터
        P_REQ_URL = P_REQ_URL + "?P_TID=" + P_TID + "&P_MID=" + P_MID;

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(P_REQ_URL);
        method
            .getParams()
            .setParameter(
                HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        HashMap<String, String> resultMap = new HashMap<>();
        try {
          int statusCode = client.executeMethod(method);
          if (statusCode != HttpStatus.OK.value()) {
            log.info("Method failed: " + method.getStatusLine());
          }

          // -------------------- 승인결과 수신 -------------------------------------------------
          byte[] responseBody = method.getResponseBody();
          String[] values = new String(responseBody).split("&");

          for (int x = 0; x < values.length; x++) {
            String[] value = values[x].split("=");
            resultMap.put(value[0], value.length >= 2 ? value[1] : null);
          }
          // log.info("resultMap : " + resultMap);

          Gson gson = new Gson();
          return gson.fromJson(gson.toJson(resultMap), InicisPayMobileResponse.class);

        } catch (HttpException e) {
          log.error("Fatal protocol violation: ", e);
        } catch (IOException e) {
          log.error("Fatal transport error: ", e);
        } finally {
          method.releaseConnection();
        }
      } else {
        log.info("Auth Fail");
        log.info("<br>");
        log.info(P_RMESG1);
      }

    } catch (Exception e) {
      log.error("payMobile error: ", e);
    }
    return null;
  }

  @Override
  public void refund(Ticket ticket) {

    Date date_now = new Date(System.currentTimeMillis());
    SimpleDateFormat fourteen_format = new SimpleDateFormat("yyyyMMddHHmmss");

    // step1. 요청을 위한 파라미터 설정
    String key = API_KEY;
    String type = "Refund";
    String paymethod = "Card";
    String timestamp = fourteen_format.format(date_now);
    String clientIp = Constants.getIP();
    String mid = MID;
    String tid = ticket.getPgId();
    String msg = "취소요청";

    // Hash Encryption
    String data_hash = key + type + paymethod + timestamp + clientIp + mid + tid;
    String hashData = hash(data_hash);

    // Request URL
    String apiUrl = "https://iniapi.inicis.com/api/v1/refund";

    Map<String, Object> resultMap = new HashMap<>();
    resultMap.put("type", type);
    resultMap.put("paymethod", paymethod);
    resultMap.put("timestamp", timestamp);
    resultMap.put("clientIp", clientIp);
    resultMap.put("mid", mid);
    resultMap.put("tid", tid);
    resultMap.put("msg", msg);
    resultMap.put("hashData", hashData);

    StringBuilder postData = new StringBuilder();
    for (Map.Entry<String, Object> params : resultMap.entrySet()) {

      if (postData.length() != 0) postData.append("&");
      try {
        postData.append(URLEncoder.encode(params.getKey(), "UTF-8"));
        postData.append("=");
        postData.append(URLEncoder.encode(String.valueOf(params.getValue()), "UTF-8"));
      } catch (Exception e) {
        throw new ApiException(ErrorCode.BAD_REQUEST);
      }
    }

    // step2. key=value 로 post 요청
    try {
      URL url = new URL(apiUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      if (conn != null) {
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        conn.setRequestMethod("POST");
        conn.setDefaultUseCaches(false);
        conn.setDoOutput(true);

        if (conn.getDoOutput()) {
          conn.getOutputStream().write(postData.toString().getBytes("UTF-8"));
          conn.getOutputStream().flush();
          conn.getOutputStream().close();
        }

        conn.connect();

        BufferedReader br =
            new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

        // step3. 요청 결과
        // System.out.println(br.readLine());
        br.close();
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new ApiException(ErrorCode.BAD_REQUEST);
    }
  }

  public String hash(String data_hash) {
    String salt = data_hash;
    String hex = null;

    try {
      MessageDigest msg = MessageDigest.getInstance("SHA-512");
      msg.update(salt.getBytes());
      hex = String.format("%128x", new BigInteger(1, msg.digest()));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return hex;
  }
}
