package art.heredium.ncloud.service.sens.biz.model.ncloud.button;

import lombok.Getter;
import lombok.Setter;

import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkButton;
import art.heredium.ncloud.service.sens.biz.type.NCloudBizAlimTalkButtonType;

@Getter
@Setter
public class NCloudBizAlimTalkButtonWL extends NCloudBizAlimTalkButton {
  public NCloudBizAlimTalkButtonWL(String name, String linkMobile, String linkPc) {
    this.type = NCloudBizAlimTalkButtonType.WL.name();
    this.name = name;
    this.linkMobile = linkMobile;
    this.linkPc = linkPc;
  }
}
