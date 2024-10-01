package art.heredium.ncloud.service.sens.biz.component;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;

import art.heredium.core.util.Constants;
import art.heredium.ncloud.error.NCloudKitErrorCode;
import art.heredium.ncloud.error.NCloudKitException;
import art.heredium.ncloud.feign.client.NCloudSENSClient;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkRequest;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkFailOverConfig;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkMessage;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkResponse;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkTemplate;

@Component
@AllArgsConstructor
public class NCloudBizAlimTalk {

  private final NCloudSENSClient client;
  private final Environment env;

  public List<NCloudBizAlimTalkResponse> send(NCloudBizAlimTalkRequest request) {
    for (NCloudBizAlimTalkMessage msg : request.getMessages()) {
      NCloudBizAlimTalkFailOverConfig failOverConfig = msg.getFailoverConfig();
      if (failOverConfig != null) {
        if (StringUtils.isNullOrEmpty(failOverConfig.getFrom())) {
          String failoverFrom = env.getProperty("ncloud.service.sens.biz.alimtalk.failover.from");
          if (failoverFrom == null) {
            throw new NCloudKitException(
                NCloudKitErrorCode.BIZ_FAILOVER_INAVLID,
                "Fail-Over 기능을 사용하려면 Biz Calling Number가 필요합니다.");
          } else {
            failOverConfig.setFrom(failoverFrom);
          }
        }
      }
    }

    List<NCloudBizAlimTalkResponse> responseList = new ArrayList<>();
    List<List<NCloudBizAlimTalkMessage>> list = Constants.separateList(request.getMessages(), 100);
    list.forEach(
        msgs -> {
          NCloudBizAlimTalkRequest cloned = new NCloudBizAlimTalkRequest(request);
          cloned.setMessages(msgs);
          NCloudBizAlimTalkResponse response = client.alimtalkSend(cloned);
          responseList.add(response);
        });
    return responseList;
  }

  public List<NCloudBizAlimTalkTemplate> getTemplate(String channelId, String templateCode) {
    return client.template(channelId, templateCode);
  }

  public void deleteReserve(String reserveId) {
    client.cancel(reserveId);
  }
}
