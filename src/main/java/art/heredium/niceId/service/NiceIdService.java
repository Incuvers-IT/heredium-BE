package art.heredium.niceId.service;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.jwt.JwtRedisUtil;
import art.heredium.niceId.model.dto.request.PostNiceIdEncryptRequest;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class NiceIdService {

    @Value("${nice-id.site-code}")
    public String SITE_CODE;

    @Value("${nice-id.site-password}")
    public String SITE_PASSWORD;

    private final JwtRedisUtil jwtRedisUtil;

    public String encrypt(PostNiceIdEncryptRequest dto) {
        NiceID.Check.CPClient niceCheck = new NiceID.Check.CPClient();

        String sRequestNumber = "REALNICEIDREQUEST001";            // 요청 번호, 이는 성공/실패후에 같은 값으로 되돌려주게 되므로
        // 업체에서 적절하게 변경하여 쓰거나, 아래와 같이 생성한다.가
        sRequestNumber = niceCheck.getRequestNO(SITE_CODE);
        jwtRedisUtil.setDataExpire(sRequestNumber, true, 30 * 60);
//        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//        attr.getRequest().getSession().setAttribute("REQ_SEQ", sRequestNumber);    // 해킹등의 방지를 위하여 세션을 쓴다면, 세션에 요청번호를 넣는다.
//        System.out.println("encrypt : " + attr.getRequest().getSession().getAttribute("REQ_SEQ"));
        String sAuthType = "";        // 없으면 기본 선택화면, M(휴대폰), X(인증서공통), U(공동인증서), F(금융인증서), S(PASS인증서), C(신용카드)
        String customize = "";        //없으면 기본 웹페이지 / Mobile : 모바일페이지

        // CheckPlus(본인인증) 처리 후, 결과 데이타를 리턴 받기위해 다음예제와 같이 http부터 입력합니다.
        //리턴url은 인증 전 인증페이지를 호출하기 전 url과 동일해야 합니다. ex) 인증 전 url : http://www.~ 리턴 url : http://www.~
        String sReturnUrl = dto.getReturnUrl();      // 성공시 이동될 URL
        String sErrorUrl = dto.getErrorUrl();          // 실패시 이동될 URL

        // 입력될 plain 데이타를 만든다.
        String sPlainData = "7:REQ_SEQ" + sRequestNumber.getBytes().length + ":" + sRequestNumber +
                            "8:SITECODE" + SITE_CODE.getBytes().length + ":" + SITE_CODE +
                            "9:AUTH_TYPE" + sAuthType.getBytes().length + ":" + sAuthType +
                            "7:RTN_URL" + sReturnUrl.getBytes().length + ":" + sReturnUrl +
                            "7:ERR_URL" + sErrorUrl.getBytes().length + ":" + sErrorUrl +
                            "9:CUSTOMIZE" + customize.getBytes().length + ":" + customize;

        String sMessage;
        int iReturn = niceCheck.fnEncode(SITE_CODE, SITE_PASSWORD, sPlainData);
        if (iReturn == 0) {
            return niceCheck.getCipherData();
        } else if (iReturn == -1) {
            sMessage = "암호화 시스템 에러입니다.";
        } else if (iReturn == -2) {
            sMessage = "암호화 처리오류입니다.";
        } else if (iReturn == -3) {
            sMessage = "암호화 데이터 오류입니다.";
        } else if (iReturn == -9) {
            sMessage = "입력 데이터 오류입니다.";
        } else {
            sMessage = "알수 없는 에러 입니다. iReturn : " + iReturn;
        }
        throw new ApiException(ErrorCode.BAD_VALID, sMessage);
    }

    public PostNiceIdEncryptResponse decrypt(String encodeData) {
        NiceID.Check.CPClient niceCheck = new NiceID.Check.CPClient();

        String sEncodeData = requestReplace(encodeData, "encodeData");

        String sMessage;
        int iReturn = niceCheck.fnDecode(SITE_CODE, SITE_PASSWORD, sEncodeData);
        if (iReturn == 0) {
            String sPlainData = niceCheck.getPlainData();
            String sCipherTime = niceCheck.getCipherDateTime(); // 복호화한 시간
            // 데이타를 추출합니다.
            HashMap mapresult = niceCheck.fnParse(sPlainData);
            PostNiceIdEncryptResponse response = new PostNiceIdEncryptResponse(mapresult);

//            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//            String session_sRequestNumber = (String) attr.getRequest().getSession().getAttribute("REQ_SEQ");
//            System.out.println("response.getRequestNumber() : " + response.getRequestNumber());
//            System.out.println("decrypt : " + session_sRequestNumber);
//            if (response.getRequestNumber() == null || session_sRequestNumber == null || !response.getRequestNumber().equals(session_sRequestNumber)) {
            if (response.getRequestNumber() == null || jwtRedisUtil.getData(response.getRequestNumber(), Boolean.class) == null) {
                sMessage = "세션값 불일치 오류입니다.";
            } else {
                return response;
            }
        } else if (iReturn == -1) {
            sMessage = "복호화 시스템 오류입니다.";
        } else if (iReturn == -4) {
            sMessage = "복호화 처리 오류입니다.";
        } else if (iReturn == -5) {
            sMessage = "복호화 해쉬 오류입니다.";
        } else if (iReturn == -6) {
            sMessage = "복호화 데이터 오류입니다.";
        } else if (iReturn == -9) {
            sMessage = "입력 데이터 오류입니다.";
        } else if (iReturn == -12) {
            sMessage = "사이트 패스워드 오류입니다.";
        } else {
            sMessage = "알수 없는 에러 입니다. iReturn : " + iReturn;
        }
        throw new ApiException(ErrorCode.BAD_VALID, sMessage);
    }

    public String requestReplace(String paramValue, String gubun) {

        String result = "";

        if (paramValue != null) {

            paramValue = paramValue.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

            paramValue = paramValue.replaceAll("\\*", "");
            paramValue = paramValue.replaceAll("\\?", "");
            paramValue = paramValue.replaceAll("\\[", "");
            paramValue = paramValue.replaceAll("\\{", "");
            paramValue = paramValue.replaceAll("\\(", "");
            paramValue = paramValue.replaceAll("\\)", "");
            paramValue = paramValue.replaceAll("\\^", "");
            paramValue = paramValue.replaceAll("\\$", "");
            paramValue = paramValue.replaceAll("'", "");
            paramValue = paramValue.replaceAll("@", "");
            paramValue = paramValue.replaceAll("%", "");
            paramValue = paramValue.replaceAll(";", "");
            paramValue = paramValue.replaceAll(":", "");
            paramValue = paramValue.replaceAll("-", "");
            paramValue = paramValue.replaceAll("#", "");
            paramValue = paramValue.replaceAll("--", "");
            paramValue = paramValue.replaceAll("-", "");
            paramValue = paramValue.replaceAll(",", "");

            if (gubun != "encodeData") {
                paramValue = paramValue.replaceAll("\\+", "");
                paramValue = paramValue.replaceAll("/", "");
                paramValue = paramValue.replaceAll("=", "");
            }

            result = paramValue;

        }
        return result;
    }
}