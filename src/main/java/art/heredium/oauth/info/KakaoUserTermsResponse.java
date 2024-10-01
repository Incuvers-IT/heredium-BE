package art.heredium.oauth.info;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserTermsResponse {
  private Long user_id;
  private List<AllowedServiceTerms> allowed_service_terms;
  private List<AppServiceTerms> app_service_terms;

  @Getter
  @Setter
  public static class AllowedServiceTerms {
    private String tag; // 서비스 약관에 설정된 태그(tag)
    private LocalDateTime agreed_at; // 사용자가 해당 약관에 마지막으로 동의한 시간
  }

  @Getter
  @Setter
  public static class AppServiceTerms {
    private String tag; // 서비스 약관에 설정된 태그(tag))
    private LocalDateTime created_at; // 서비스 약관이 등록된 시간
    private LocalDateTime updated_at; // 서비스 약관이 수정된 시간
  }
}
