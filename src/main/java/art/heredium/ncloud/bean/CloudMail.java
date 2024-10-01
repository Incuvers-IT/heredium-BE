package art.heredium.ncloud.bean;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import org.apache.tomcat.util.codec.binary.Base64;

import art.heredium.core.util.Constants;
import art.heredium.ncloud.model.EmailWithParameter;
import art.heredium.ncloud.model.Message;
import art.heredium.ncloud.model.RecipientForRequest;
import art.heredium.ncloud.model.ReserveModel;
import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSResponse;
import art.heredium.ncloud.type.MailTemplate;

@Slf4j
@Component
public class CloudMail {

  @Value("${spring.profiles.active}")
  public String ACTIVE;

  @Value("${ncloud.credentials.access-key}")
  public String ACCESS_KEY;

  @Value("${ncloud.credentials.secret-key}")
  public String SECRET_KEY;

  @Value("${ncloud.service.sens.sms.service-id}")
  public String SMS_SERVICE_ID;

  @Value("${ncloud.service.sens.sms.from}")
  public String SMS_FROM;

  @Value("${ncloud.service.sens.url}")
  public String API_SMS_URL;

  @Value("${ncloud.service.mail.url}")
  public String API_MAIL_URL;

  private String makeSignature(HttpMethod method, String epochTimestamps, String url)
      throws Exception {
    String space = " "; // 공백
    String newLine = "\n"; // 줄바꿈
    String timestamp = epochTimestamps; // 현재 타임스탬프 (epoch, millisecond)

    String message =
        new StringBuilder()
            .append(method.name().toUpperCase(Locale.ROOT))
            .append(space)
            .append(url)
            .append(newLine)
            .append(timestamp)
            .append(newLine)
            .append(ACCESS_KEY)
            .toString();

    SecretKeySpec signingKey =
        new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(signingKey);

    byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

    return Base64.encodeBase64String(rawHmac);
  }

  private HttpHeaders getNCloudHeader(String apiUrl, String epochTimestamps) throws Exception {
    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_JSON);
    header.add("x-ncp-apigw-timestamp", epochTimestamps);
    header.add("x-ncp-iam-access-key", ACCESS_KEY);
    header.add("x-ncp-apigw-signature-v2", makeSignature(HttpMethod.POST, epochTimestamps, apiUrl));
    return header;
  }

  public void mail(String email, Map<String, String> replace, MailTemplate template) {
    try {
      String url = API_MAIL_URL;
      String apiUrl = "/api/v1/mails";
      String epochTimestamps = String.valueOf(Instant.now().toEpochMilli());
      HttpHeaders header = getNCloudHeader(apiUrl, epochTimestamps);

      List<RecipientForRequest> receivers = new ArrayList<>();
      RecipientForRequest r = new RecipientForRequest();
      r.setAddress(email);
      r.setName(email);
      receivers.add(r);

      Map<String, Object> map = new HashMap<>();
      map.put("templateSid", template.getId(ACTIVE));
      map.put("parameters", replace);
      map.put("recipients", receivers);

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, header);

      UriComponents uri = UriComponentsBuilder.fromHttpUrl(url + apiUrl).build();

      HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
      factory.setConnectTimeout(5000);
      factory.setReadTimeout(5000);
      RestTemplate restTemplate = new RestTemplate(factory);
      ResponseEntity<Map> resultMap = restTemplate.postForEntity(uri.toString(), entity, Map.class);
      //            log.info(resultMap.toString());
    } catch (Exception e) {
      log.error("CloudMail-template", e);
    }
  }

  public void mail(List<EmailWithParameter> emails, MailTemplate template) {
    try {
      String url = API_MAIL_URL;
      String apiUrl = "/api/v1/mails";
      String epochTimestamps = String.valueOf(Instant.now().toEpochMilli());
      HttpHeaders header = getNCloudHeader(apiUrl, epochTimestamps);

      List<RecipientForRequest> receivers = new ArrayList<>();
      emails.forEach(
          x -> {
            RecipientForRequest r = new RecipientForRequest();
            r.setAddress(x.getEmail());
            r.setParameters(x.getParameters());
            receivers.add(r);
          });

      Map<String, Object> map = new HashMap<>();
      map.put("templateSid", template.getId(ACTIVE));
      map.put("recipients", receivers);

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, header);

      UriComponents uri = UriComponentsBuilder.fromHttpUrl(url + apiUrl).build();

      HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
      factory.setConnectTimeout(5000);
      factory.setReadTimeout(5000);
      RestTemplate restTemplate = new RestTemplate(factory);
      ResponseEntity<Map> resultMap = restTemplate.postForEntity(uri.toString(), entity, Map.class);
      //            log.info(resultMap.toString());
    } catch (Exception e) {
      log.error("CloudMail-template", e);
    }
  }

  public List<String> sms(List<ReserveModel> list, LocalDateTime reserveTime) {
    List<List<ReserveModel>> separateList = Constants.separateList(list, 100);
    return separateList.stream().map(x -> smsRequest(x, reserveTime)).collect(Collectors.toList());
  }

  public void sms(List<ReserveModel> list) {
    sms(list, null);
  }

  public void sms(ReserveModel reserveModel) {
    List<ReserveModel> list = new ArrayList<>();
    list.add(reserveModel);
    sms(list);
  }

  public List<String> sms(ReserveModel reserveModel, LocalDateTime reserveTime) {
    List<ReserveModel> list = new ArrayList<>();
    list.add(reserveModel);
    return sms(list, reserveTime);
  }

  private String smsRequest(List<ReserveModel> list, LocalDateTime reserveTime) {
    try {
      String url = API_SMS_URL;
      String apiUrl = String.format("/sms/v2/services/%s/messages", SMS_SERVICE_ID);
      String epochTimestamps = String.valueOf(Instant.now().toEpochMilli());
      HttpHeaders header = getNCloudHeader(apiUrl, epochTimestamps);

      List<Message> receivers =
          list.stream()
              .map(
                  m ->
                      new Message(
                          m.getPhone().replace("-", "").trim(), m.getSubject(), m.getContents()))
              .collect(Collectors.toList());

      Map<String, Object> map = new HashMap<>();
      map.put("type", "LMS");
      map.put("contentType", "COMM");
      map.put("countryCode", "82");
      map.put("from", SMS_FROM);
      map.put("subject", "subject");
      map.put("content", "content");
      map.put("messages", receivers);
      if (reserveTime != null) {
        map.put("reserveTime", reserveTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
      }

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, header);
      UriComponents uri = UriComponentsBuilder.fromHttpUrl(url + apiUrl).build();

      HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
      factory.setConnectTimeout(5000);
      factory.setReadTimeout(5000);
      RestTemplate restTemplate = new RestTemplate(factory);
      ResponseEntity<NCloudSMSResponse> result =
          restTemplate.postForEntity(uri.toString(), entity, NCloudSMSResponse.class);
      if (result.getBody() != null) {
        return result.getBody().getRequestId();
      }
    } catch (Exception e) {
      log.error("CloudMail-sms", e);
    }
    return null;
  }

  private String smsReserveCancel(String smsRequestId) {
    try {
      String url = API_SMS_URL;
      String apiUrl =
          String.format("/sms/v2/services/%s/reservations/%s", SMS_SERVICE_ID, smsRequestId);
      String epochTimestamps = String.valueOf(Instant.now().toEpochMilli());
      HttpHeaders header = getNCloudHeader(apiUrl, epochTimestamps);

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(null, header);
      UriComponents uri = UriComponentsBuilder.fromHttpUrl(url + apiUrl).build();

      HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
      factory.setConnectTimeout(5000);
      factory.setReadTimeout(5000);
      RestTemplate restTemplate = new RestTemplate(factory);
      ResponseEntity<NCloudSMSResponse> result =
          restTemplate.postForEntity(uri.toString(), entity, NCloudSMSResponse.class);
      if (result.getBody() != null) {
        return result.getBody().getRequestId();
      }
    } catch (Exception e) {
      log.error("CloudMail-smsReserveCancel", e);
    }
    return null;
  }

  public void smsReserveCancel(List<String> smsRequestId) {
    smsRequestId.forEach(x -> smsReserveCancel(x));
  }
}
