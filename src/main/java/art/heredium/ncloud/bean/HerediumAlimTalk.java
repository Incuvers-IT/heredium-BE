package art.heredium.ncloud.bean;

import art.heredium.ncloud.service.sens.biz.component.NCloudBizAlimTalk;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkBuilder;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkMessageBuilder;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkFailOverConfig;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkMessage;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkResponse;
import art.heredium.ncloud.type.AlimTalkTemplate;
import art.heredium.core.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HerediumAlimTalk {

    @Value("${spring.profiles.active}")
    public String ACTIVE;

    private final NCloudBizAlimTalk bizAlimTalkService;

    public List<String> sendAlimTalk(List<NCloudBizAlimTalkMessage> messages, AlimTalkTemplate template, LocalDateTime reserveTime) {
        try {
            NCloudBizAlimTalkBuilder builder = new NCloudBizAlimTalkBuilder(template.getPlusFriendId(ACTIVE), template.getTemplateCode(ACTIVE));
            if (reserveTime != null) {
                if (!Constants.getNow().plusMinutes(10).isBefore(reserveTime)) {
                    return null;
                }
                builder.useReserve(reserveTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            builder.messages(messages);
            return bizAlimTalkService.send(builder.build()).stream().map(NCloudBizAlimTalkResponse::getRequestId).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> sendAlimTalk(List<NCloudBizAlimTalkMessage> messages, AlimTalkTemplate template) {
        return sendAlimTalk(messages, template, null);
    }

    public List<String> sendAlimTalk(String to, Map<String, String> variables, AlimTalkTemplate template, LocalDateTime reserveTime) {
        NCloudBizAlimTalkMessage message = new NCloudBizAlimTalkMessageBuilder()
                .variables(variables)
                .to(to)
                .title(template.getTitle())
                .failOver(new NCloudBizAlimTalkFailOverConfig())
                .build();
        return sendAlimTalk(Arrays.asList(message), template, reserveTime);
    }

    public List<String> sendAlimTalk(String to, Map<String, String> variable, AlimTalkTemplate template) {
        return sendAlimTalk(to, variable, template, null);
    }

    public void cancelAlimTalk(List<String> smsRequestId) {
        if (smsRequestId != null && smsRequestId.size() > 0) {
            smsRequestId.forEach(requestId -> {
                try {
                    bizAlimTalkService.deleteReserve(requestId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
