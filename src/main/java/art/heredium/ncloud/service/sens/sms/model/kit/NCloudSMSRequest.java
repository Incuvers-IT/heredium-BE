package art.heredium.ncloud.service.sens.sms.model.kit;

import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSMessage;
import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSRequestModel;
import com.amazonaws.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import art.heredium.ncloud.error.NCloudKitErrorCode;
import art.heredium.ncloud.error.NCloudKitException;

import java.io.UnsupportedEncodingException;

@Getter
@Setter
public class NCloudSMSRequest extends NCloudSMSRequestModel {

    public NCloudSMSRequest(NCloudSMSRequestModel builder) {
        super(builder);
    }

    public void addReceiver(String to) {
        this.addReceiver(to, this.subject, this.content);
    }

    public void addReceiver(String to, String content) {
        this.addReceiver(to, this.subject, content);
    }

    public void addReceiver(String to, String subject, String content) {
        if (StringUtils.isNullOrEmpty(to)) {
            throw new NCloudKitException(NCloudKitErrorCode.SMS_TO_INVALID, "받는이의 연락처는 NULL 혹은 비어있을 수 없습니다.");
        }

        if (StringUtils.isNullOrEmpty(content) && StringUtils.isNullOrEmpty(this.content)) {
            throw new NCloudKitException(NCloudKitErrorCode.SMS_CONTENT_INVALID, "받는이의 문자 내용은 NULL 혹은 비어있을 수 없습니다. 대안으로 기본 컨텐츠를 삽입해주세요.");
        }

        try {
            if(this.content.getBytes("EUC-KR").length > 80){
                this.type = "LMS";
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        NCloudSMSMessage msg = new NCloudSMSMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setContent(content);

        this.messages.add(msg);
    }


}
