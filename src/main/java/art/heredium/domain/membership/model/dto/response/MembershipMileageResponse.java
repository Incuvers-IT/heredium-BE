package art.heredium.domain.membership.model.dto.response;

import art.heredium.domain.membership.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipMileageResponse {

  @JsonProperty("id")
  private Long id;                            // PK

  @JsonProperty("accountId")
  private Long accountId;                     // 사용자ID

  @JsonProperty("type")
  private Integer type;                          // 이벤트타입

  @JsonProperty("category")
  private Integer category;                      // 대분류

  @JsonProperty("categoryId")
  private Long categoryId;                    // 대분류대상

  @JsonProperty("paymentMethod")
  private Integer paymentMethod;                 // 결제방법

  @JsonProperty("paymentAmount")
  private Integer paymentAmount;              // 결제금액

  @JsonProperty("serialNumber")
  private String serialNumber;                // 일련번호

  @JsonProperty("mileageAmount")
  private Integer mileageAmount;                // 마일리지

  @JsonProperty("expirationDate")
  private LocalDateTime expirationDate;       // 만료일

  @JsonProperty("createdName")
  private String createdName;             // 생성자

  @JsonProperty("createdDate")
  private LocalDateTime createdDate;       // 생성일

  @JsonProperty("lastModifiedName")
  private String lastModifiedName;        // 수정자

  @JsonProperty("lastModifiedDate")
  private LocalDateTime lastModifiedDate;  // 수정일

  @JsonProperty("title")
  private String title;                // 카테고리 제목
}