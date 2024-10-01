package art.heredium.ncloud.service.sens.sms.model.ncloud;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NCloudSMSResponse {
  private String requestId;
  private String requestTime;
  private String statusCode;
  private String statusName;
}
