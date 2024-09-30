package art.heredium.ncloud.service.sens.biz.model.ncloud;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NCloudBizAlimTalkTemplate {
    private String createTime;
    private String updateTime;
    private String channelId;
    private String templateCode;
    private String templateName;
    private String content;
    private List<NCloudBizAlimTalkTemplateComment> comments;
    private String templateInspectionStatus;
    private String templateStatus;
    private List<NCloudBizAlimTalkButton> buttons;
}
