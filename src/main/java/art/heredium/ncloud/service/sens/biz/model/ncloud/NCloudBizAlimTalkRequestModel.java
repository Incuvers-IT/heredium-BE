package art.heredium.ncloud.service.sens.biz.model.ncloud;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NCloudBizAlimTalkRequestModel {
  protected String plusFriendId;
  protected String templateCode;
  protected List<NCloudBizAlimTalkMessage> messages;
  protected String reserveTime;
  protected String reserveTimeZone;
  protected String scheduleCode;

  public NCloudBizAlimTalkRequestModel(NCloudBizAlimTalkRequestModel builder) {
    this.plusFriendId = builder.getPlusFriendId();
    this.templateCode = builder.getTemplateCode();
    this.messages = builder.getMessages();
    this.reserveTime = builder.getReserveTime();
    this.reserveTimeZone = builder.getReserveTimeZone();
    this.scheduleCode = builder.getScheduleCode();
  }

  public void setMessages(List<NCloudBizAlimTalkMessage> messages) {
    this.messages = messages;
  }
}
