package art.heredium.ncloud.service.sens.sms.model.ncloud;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NCloudSMSMessage {
  private String to;
  private String subject;
  private String content;
}
