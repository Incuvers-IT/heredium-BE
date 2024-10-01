package art.heredium.ncloud.service.sens.sms.component;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import art.heredium.core.util.Constants;
import art.heredium.ncloud.feign.client.NCloudSENSClient;
import art.heredium.ncloud.service.sens.sms.model.kit.NCloudSMSRequest;
import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSMessage;
import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSResponse;

/** Docs: <a href="https://api.ncloud-docs.com/docs/ai-application-service-sens-smsv2">...</a> */
@Slf4j
@Component
@AllArgsConstructor
public class NCloudSMS {

  private final NCloudSENSClient nCloudSENSClient;

  public List<NCloudSMSResponse> send(NCloudSMSRequest request) {
    List<NCloudSMSResponse> responseList = new ArrayList<>();
    List<List<NCloudSMSMessage>> list = Constants.separateList(request.getMessages(), 100);
    list.forEach(
        msgs -> {
          NCloudSMSRequest cloned = new NCloudSMSRequest(request);
          cloned.setMessages(msgs);
          NCloudSMSResponse response = nCloudSENSClient.smsSend(cloned);
          responseList.add(response);
        });
    return responseList;
  }
}
