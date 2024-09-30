package art.heredium.ncloud.service.sens.biz.model.ncloud.button;

import lombok.Getter;
import lombok.Setter;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkButton;
import art.heredium.ncloud.service.sens.biz.type.NCloudBizAlimTalkButtonType;

@Getter
@Setter
public class NCloudBizAlimTalkButtonAL extends NCloudBizAlimTalkButton {
    public NCloudBizAlimTalkButtonAL(String name, String schemeIos, String schemeAndroid){
        this.type = NCloudBizAlimTalkButtonType.AL.name();
        this.name = name;
        this.schemeIos = schemeIos;
        this.schemeAndroid = schemeAndroid;
    }
}
