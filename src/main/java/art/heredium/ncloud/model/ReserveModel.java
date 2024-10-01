package art.heredium.ncloud.model;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import art.heredium.ncloud.type.SmsTemplate;

@Getter
@Setter
public class ReserveModel {
  private String phone;
  private String subject;
  private String contents;

  public ReserveModel(String phone, SmsTemplate template, Map<String, String> param) {
    this.phone = phone;
    this.subject = template.getTitle();
    this.contents = template.getContents();
    param.forEach(
        (key, value) -> this.contents = this.contents.replace(String.format("${%s}", key), value));
  }
}
