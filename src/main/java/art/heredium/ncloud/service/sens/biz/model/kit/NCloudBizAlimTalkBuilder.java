package art.heredium.ncloud.service.sens.biz.model.kit;

import art.heredium.ncloud.feign.client.NCloudSENSClient;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkMessage;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkRequestModel;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import art.heredium.core.config.spring.ApplicationBeanUtil;
import art.heredium.ncloud.error.NCloudKitErrorCode;
import art.heredium.ncloud.error.NCloudKitException;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class NCloudBizAlimTalkBuilder extends NCloudBizAlimTalkRequestModel {

    @JsonIgnore
    private NCloudSENSClient client;

    @JsonIgnore
    private NCloudBizAlimTalkTemplate template;

    @JsonIgnore
    private String templateContent;

    public NCloudBizAlimTalkBuilder(String channelId, String templateCode) {
        this.plusFriendId = channelId;
        this.templateCode = templateCode;

        this.client = (NCloudSENSClient) ApplicationBeanUtil.getBean(NCloudSENSClient.class.getSimpleName());
        List<NCloudBizAlimTalkTemplate> templates = this.client.template(channelId, templateCode);
        if (templates == null || templates.size() == 0)
            throw new NCloudKitException(NCloudKitErrorCode.BIZ_TEMPLATE_INVALID, "알림톡 템플릿을 찾을 수 없습니다.");
        this.template = templates.get(0);
    }

    public NCloudBizAlimTalkBuilder message(NCloudBizAlimTalkMessage message) {
        if (this.messages == null)
            this.messages = new ArrayList<>();
        message.setButtons(this.template.getButtons());
        this.messages.add(message);
        return this;
    }

    public NCloudBizAlimTalkBuilder messages(List<NCloudBizAlimTalkMessage> messages) {
        messages.forEach(message -> message.setButtons(this.template.getButtons()));
        this.messages = messages;
        return this;
    }

    /**
     * 기본 타임존인 Asia/Seoul을 사용할 경우
     *
     * @param reserveTime : 메시지 발송 예약 일시 (yyyy-MM-dd HH:mm)
     * @return
     */
    public NCloudBizAlimTalkBuilder useReserve(String reserveTime) {
        this.reserveTime = reserveTime;
        return this;
    }

    /**
     * @param reserveTime     : 메시지 발송 예약 일시 (yyyy-MM-dd HH:mm)
     * @param reserveTimeZone : - 예약 일시 타임존 (기본: Asia/Seoul)
     * @return
     * @link 지원 타임존 목록 : https://en.wikipedia.org/wiki/List_of_tz_database_time_zones, TZ database name 값 사용
     */
    public NCloudBizAlimTalkBuilder useReserve(String reserveTime, String reserveTimeZone) {
        this.reserveTime = reserveTime;
        this.reserveTimeZone = reserveTimeZone;
        return this;
    }

    /**
     * @param scheduleCode : NCloud 관리자에서 {Service}-Reservation 메뉴에서 스케줄 생성한 코드
     * @return
     */
    public NCloudBizAlimTalkBuilder useSchedule(String scheduleCode) {
        this.scheduleCode = scheduleCode;
        return this;
    }

    private void variableContentReplace() {
        for (NCloudBizAlimTalkMessage message : this.messages) {
            List<String> variables = new ArrayList<>();
            Pattern pattern = Pattern.compile("#\\{(.*?)}");
            Matcher matcher = pattern.matcher(this.template.getContent());
            while (matcher.find()) {
                variables.add(matcher.group(1));
            }

            String content = this.template.getContent();
            for (String key : variables) {
                String replaceValue = message.getVariables().get(key);
                content = content.replace(String.format("#{%s}", key), replaceValue);
            }
            try {
                if (content.getBytes("EUC-KR").length > 80) {
                    message.getFailoverConfig().setType("LMS");
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            message.setContent(content);
        }
    }

    public NCloudBizAlimTalkRequest build() {
        variableContentReplace();
        return new NCloudBizAlimTalkRequest(this);
    }
}
