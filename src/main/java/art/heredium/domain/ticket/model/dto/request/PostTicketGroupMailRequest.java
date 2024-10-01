package art.heredium.domain.ticket.model.dto.request;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.ticket.type.TicketKindType;

@Getter
@Setter
public class PostTicketGroupMailRequest {
  @NotNull private TicketKindType kind;
  @NotEmpty private String title;
  @NotNull private LocalDateTime date;
  @NotNull private Integer number;
  @NotBlank private String name;
  @NotBlank private String reader;
  @NotBlank private String tel;
  @NotNull private String email;
  @NotNull private String note;

  public Map<String, String> toMap(
      UserPrincipal userPrincipal, HerediumProperties herediumProperties) {
    Map<String, String> map = new HashMap<>();
    if (userPrincipal != null) {
      map.put("accountEmail", userPrincipal.getEmail());
      map.put("accountName", userPrincipal.getName());
      map.put("accountTel", userPrincipal.getAccount().getAccountInfo().getPhone());
    } else {
      map.put("accountEmail", "-");
      map.put("accountName", "");
      map.put("accountTel", "-");
    }
    map.put("title", title);
    map.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    map.put("hour", date.format(DateTimeFormatter.ofPattern("HH")));
    map.put("number", Constants.phone(number.toString()));
    map.put("name", name);
    map.put("reader", reader);
    map.put("tel", tel);
    map.put("email", email);
    map.put("note", note);
    map.put("CSTel", herediumProperties.getTel());
    map.put("CSEmail", herediumProperties.getEmail());
    return map;
  }
}
