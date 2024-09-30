package art.heredium.ncloud.service.sens.biz.model.ncloud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkMessageBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class NCloudBizAlimTalkMessage {
    protected String countryCode;
    protected String to;
    protected String title;
    protected String content;
    protected String headerContent;
    protected NCloudBizAlimTalkArticle itemHighlight;
    protected NCloudBizAlimTalkItem item;
    protected List<NCloudBizAlimTalkButton> buttons;
    protected Boolean useSmsFailover;
    protected NCloudBizAlimTalkFailOverConfig failoverConfig;

    @JsonIgnore
    protected Map<String, String> variables;

    public NCloudBizAlimTalkMessage(NCloudBizAlimTalkMessageBuilder builder) {
        this.countryCode = builder.getCountryCode();
        this.to = builder.getTo();
        this.title = builder.getTitle();
        this.content = builder.getContent();
        this.headerContent = builder.getHeaderContent();
        this.itemHighlight = builder.getItemHighlight();
        this.item = builder.getItem();
        this.buttons = builder.getButtons();
        this.useSmsFailover = builder.getUseSmsFailover();
        this.failoverConfig = builder.getFailoverConfig();

        this.variables = builder.getVariables();
    }
}
