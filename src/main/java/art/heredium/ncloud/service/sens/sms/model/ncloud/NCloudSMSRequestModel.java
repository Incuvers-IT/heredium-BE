package art.heredium.ncloud.service.sens.sms.model.ncloud;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NCloudSMSRequestModel {
  protected String type = "SMS"; // SMS, LMS, MMS
  protected String contentType = "COMM"; // - COMM: 일반메시지 - AD: 광고메시지- default: COMM
  protected String countrycode; // 82
  protected String from = "023339030"; // 발신번호
  protected String subject; // 제목
  protected String content; // 내용
  protected List<NCloudSMSMessage> messages = new ArrayList<>(); // 개별 전송
  protected List<NCloudSMSFile> files;
  protected String reserveTime;
  protected String reserveTimeZone;
  protected String scheduleCode;

  public NCloudSMSRequestModel(NCloudSMSRequestModel clone) {
    this.type = clone.type;
    this.contentType = clone.contentType;
    this.countrycode = clone.countrycode;
    this.from = clone.from;
    this.subject = clone.subject;
    this.content = clone.content;
    this.messages = clone.messages;
    this.files = clone.files;
    this.reserveTime = clone.reserveTime;
    this.reserveTimeZone = clone.reserveTimeZone;
    this.scheduleCode = clone.scheduleCode;
  }

  public void setMessages(List<NCloudSMSMessage> messages) {
    this.messages = messages;
  }
}
