package art.heredium.ncloud.bean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import art.heredium.core.util.Constants;
import art.heredium.ncloud.service.sens.biz.component.NCloudBizAlimTalk;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkBuilder;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkMessageBuilder;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkRequest;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkFailOverConfig;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkMessage;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkResponse;
import art.heredium.ncloud.type.AlimTalkTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class HerediumAlimTalk {

  @Value("${spring.profiles.active}")
  public String ACTIVE;

  private final NCloudBizAlimTalk bizAlimTalkService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<String> sendAlimTalk(
      List<NCloudBizAlimTalkMessage> messages,
      AlimTalkTemplate template,
      LocalDateTime reserveTime) {
    try {
      NCloudBizAlimTalkBuilder builder =
          new NCloudBizAlimTalkBuilder(
              template.getPlusFriendId(ACTIVE), template.getTemplateCode(ACTIVE));
      if (reserveTime != null) {
        if (!Constants.getNow().plusMinutes(10).isBefore(reserveTime)) {
          return null;
        }
        builder.useReserve(reserveTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
      }
      builder.messages(messages);
      final NCloudBizAlimTalkRequest request = builder.build();
      try {
        final String requestStr = this.objectMapper.writeValueAsString(request);
        log.info("SNT sendAlimTalk {}", requestStr);
      } catch (JsonProcessingException e) {
        log.error("Error deserialize NCloudBizAlimTalkRequest ", e);
      }

      final List<NCloudBizAlimTalkResponse> nCloudBizAlimTalkResponses =
          bizAlimTalkService.send(request);
      log.info(
          "RCV nCloudBizAlimTalkResponses {}"
              + this.objectMapper.writeValueAsString(nCloudBizAlimTalkResponses));
      final List<String> results =
          nCloudBizAlimTalkResponses.stream()
              .map(NCloudBizAlimTalkResponse::getRequestId)
              .collect(Collectors.toList());
      log.info("RCV sendAlimTalk {}", results);
      return results;
    } catch (Exception e) {
      log.error("Error sendAlimTalk " + e + " " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public List<String> sendAlimTalk(
      List<NCloudBizAlimTalkMessage> messages, AlimTalkTemplate template) {
    return sendAlimTalk(messages, template, null);
  }

  public List<String> sendAlimTalk(
      String to,
      Map<String, String> variables,
      AlimTalkTemplate template,
      LocalDateTime reserveTime) {
    NCloudBizAlimTalkMessage message =
        new NCloudBizAlimTalkMessageBuilder()
            .variables(variables)
            .to(to)
            .title(template.getTitle())
            .failOver(new NCloudBizAlimTalkFailOverConfig())
            .build();
    return sendAlimTalk(Arrays.asList(message), template, reserveTime);
  }

  public List<String> sendAlimTalkWithoutTitle(
      String to, Map<String, String> variables, AlimTalkTemplate template) {
    NCloudBizAlimTalkMessage message =
        new NCloudBizAlimTalkMessageBuilder()
            .variables(variables)
            .to(to)
            .failOver(new NCloudBizAlimTalkFailOverConfig())
            .build();
    return sendAlimTalk(Arrays.asList(message), template, null);
  }

  public List<String> sendAlimTalkWithoutTitle(
      Map<String, Map<String, String>> phonesAndMessagesToSendAlimTalk, AlimTalkTemplate template) {
    List<NCloudBizAlimTalkMessage> messages =
        phonesAndMessagesToSendAlimTalk.entrySet().stream()
            .map(
                entry ->
                    new NCloudBizAlimTalkMessageBuilder()
                        .variables(entry.getValue())
                        .to(entry.getKey())
                        .failOver(new NCloudBizAlimTalkFailOverConfig())
                        .build())
            .collect(Collectors.toList());
    return sendAlimTalk(messages, template, null);
  }

  public List<String> sendAlimTalkWithoutTitle(
      String to, List<Map<String, String>> params, AlimTalkTemplate template) {
    List<NCloudBizAlimTalkMessage> messages =
        params.stream()
            .map(
                variables ->
                    new NCloudBizAlimTalkMessageBuilder()
                        .variables(variables)
                        .to(to)
                        .failOver(new NCloudBizAlimTalkFailOverConfig())
                        .build())
            .collect(Collectors.toList());
    return sendAlimTalk(messages, template, null);
  }

  public List<String> sendAlimTalk(
      String to, Map<String, String> variable, AlimTalkTemplate template) {
    return sendAlimTalk(to, variable, template, null);
  }

  public void cancelAlimTalk(List<String> smsRequestId) {
    if (smsRequestId != null && smsRequestId.size() > 0) {
      smsRequestId.forEach(
          requestId -> {
            try {
              bizAlimTalkService.deleteReserve(requestId);
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
    }
  }
}
