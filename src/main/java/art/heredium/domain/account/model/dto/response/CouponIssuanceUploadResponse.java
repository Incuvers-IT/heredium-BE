package art.heredium.domain.account.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssuanceUploadResponse {
  private Long id;
  private String email;
  private String name;
  private String phone;
  private LocalDateTime createdDate;
  private LocalDateTime lastLoginDate;
  private String membershipName;
  private Long numberOfEntries;
}
