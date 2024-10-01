package art.heredium.ncloud.service.sens.biz.model.ncloud.button;

import lombok.Getter;
import lombok.Setter;

import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkButton;
import art.heredium.ncloud.service.sens.biz.type.NCloudBizAlimTalkButtonType;

@Getter
@Setter
public class NCloudBizAlimTalkButtonBK extends NCloudBizAlimTalkButton {
  public NCloudBizAlimTalkButtonBK(String name) {
    this.type = NCloudBizAlimTalkButtonType.BK.name();
    this.name = name;
  }
}
