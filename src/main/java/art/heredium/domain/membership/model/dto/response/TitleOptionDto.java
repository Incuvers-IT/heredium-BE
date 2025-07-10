package art.heredium.domain.membership.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TitleOptionDto {

  @JsonProperty("id")
  private Long id;                          // ID

  @JsonProperty("title")
  private String title;                     // 제목

}