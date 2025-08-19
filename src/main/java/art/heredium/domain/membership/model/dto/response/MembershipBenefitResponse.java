package art.heredium.domain.membership.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MembershipBenefitResponse {

    private List<Row> items;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Row {
        @JsonProperty("id") private Long membershipId;
        private Integer code;
        private String name;
        @JsonProperty("short_name") private String shortName;
        @JsonProperty("image_url")  private String imageUrl;
        @JsonProperty("usage_threshold") private Integer usageThreshold;
        private List<Coupon> coupons;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Coupon {
        private Long id;
        private String name;
        private String type;
        @JsonProperty("discount_percent") private Integer discountPercent;
        @JsonProperty("period_in_days")  private Integer periodInDays;
        @JsonProperty("image_url")       private String imageUrl;
        @JsonProperty("membership_id")   private Long membershipId;
        @JsonProperty("from_source")     private String fromSource;
    }
}