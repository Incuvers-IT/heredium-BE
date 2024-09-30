package art.heredium.ncloud.feign.client;

import art.heredium.ncloud.feign.config.NCloudFeignConfig;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkRequest;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkResponse;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkTemplate;
import art.heredium.ncloud.service.sens.sms.model.kit.NCloudSMSRequest;
import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "ncloud",
        url = "${ncloud.service.sens.url}",
        configuration = NCloudFeignConfig.class,
        qualifiers = {"NCloudSENSClient"}
)
public interface NCloudSENSClient {

    @PostMapping("/sms/v2/services/${ncloud.service.sens.sms.service-id}/messages")
    NCloudSMSResponse smsSend(@RequestBody NCloudSMSRequest requestBody);

    @PostMapping("/alimtalk/v2/services/${ncloud.service.sens.biz.service-id}/messages")
    NCloudBizAlimTalkResponse alimtalkSend(@RequestBody NCloudBizAlimTalkRequest requestBody);

    @GetMapping("/alimtalk/v2/services/${ncloud.service.sens.biz.service-id}/templates")
    List<NCloudBizAlimTalkTemplate> template(@RequestParam("channelId") String channelId, @RequestParam("templateCode") String templateCode);

    @DeleteMapping("/alimtalk/v2/services/${ncloud.service.sens.biz.service-id}/reservations/{reserveId}")
    void cancel(@PathVariable("reserveId") String reserveId);
}
