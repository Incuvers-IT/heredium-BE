package art.heredium.ncloud.service.sens.biz.model.kit;

import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkButton;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkFailOverConfig;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
public class NCloudBizAlimTalkMessageBuilder extends NCloudBizAlimTalkMessage {

    public NCloudBizAlimTalkMessageBuilder variables(Map<String, String> variables) {
        this.variables = variables;
        return this;
    }

    public NCloudBizAlimTalkMessageBuilder countryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public NCloudBizAlimTalkMessageBuilder to(String to) {
        this.to = to;
        return this;
    }

    public NCloudBizAlimTalkMessageBuilder title(String title) {
        this.title = title;
        return this;
    }

    public NCloudBizAlimTalkMessageBuilder headerContent(String headerContent) {
        this.headerContent = headerContent;
        return this;
    }

    public NCloudBizAlimTalkMessageBuilder button(NCloudBizAlimTalkButton button) {
        if (this.buttons == null)
            setButtons(new ArrayList<>());
        this.buttons.add(button);
        return this;
    }

    public NCloudBizAlimTalkMessageBuilder failOver(NCloudBizAlimTalkFailOverConfig config) {
        if (config != null) {
            this.useSmsFailover = true;
            if (config.getSubject() == null) {
                config.setSubject(this.title);
            }
            if (config.getContent() == null) {
                config.setContent(this.content);
            }
        }
        this.failoverConfig = config;
        return this;
    }

    public NCloudBizAlimTalkMessage build() {
        return new NCloudBizAlimTalkMessage(this);
    }
}
