package art.heredium.ncloud.service.sens.biz.model.ncloud.button;

import lombok.Getter;
import lombok.Setter;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkButton;
import art.heredium.ncloud.service.sens.biz.type.NCloudBizAlimTalkButtonType;

@Getter
@Setter
public class NCloudBizAlimTalkButtonAC extends NCloudBizAlimTalkButton {
    public NCloudBizAlimTalkButtonAC(){
        this.type = NCloudBizAlimTalkButtonType.AC.name();
        this.name = "채널 추가";
    }
}
