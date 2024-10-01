package art.heredium.ncloud.service.sens.biz.model.ncloud;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NCloudBizAlimTalkFailOverConfig {
  private String type = "SMS";
  private String from = "";
  private String subject;
  private String content;
}
