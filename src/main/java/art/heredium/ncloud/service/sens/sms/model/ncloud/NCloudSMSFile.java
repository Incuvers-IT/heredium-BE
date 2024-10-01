package art.heredium.ncloud.service.sens.sms.model.ncloud;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NCloudSMSFile {
  private String name;
  private String body;

  public NCloudSMSFile(String name, String body) {
    this.name = name;
    this.body = body;
  }
}
