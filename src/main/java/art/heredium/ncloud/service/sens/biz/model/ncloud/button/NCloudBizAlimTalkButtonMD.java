package art.heredium.ncloud.service.sens.biz.model.ncloud.button;

import lombok.Getter;
import lombok.Setter;

import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkButton;
import art.heredium.ncloud.service.sens.biz.type.NCloudBizAlimTalkButtonType;

@Getter
@Setter
public class NCloudBizAlimTalkButtonMD extends NCloudBizAlimTalkButton {
  public NCloudBizAlimTalkButtonMD(String name) {
    this.type = NCloudBizAlimTalkButtonType.MD.name();
    this.name = name;
  }
}
