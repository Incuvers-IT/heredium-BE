package art.heredium.ncloud.service.sens.biz.model.ncloud;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NCloudBizAlimTalkResponse {

    private String requestId;
    private String requestTime;
    private String statusCode;
    private String statusName;
    private List<Messages> messages;

    @Getter
    @Setter
    public static class Messages {
        private String messageId;
        private String countryCode;
        private String to;
        private String content;
        private String requestStatusCode;
        private String requestStatusName;
        private String requestStatusDesc;
        private String useSmsFailover;
    }
}
