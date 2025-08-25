package art.heredium.domain.coupon.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter; import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class UserCouponUsageRequest {
    /** total | available | used (기본 total) */
    private String tab = "total";

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public boolean isAvailableTab() { return "available".equalsIgnoreCase(tab); }
    public boolean isUsedTab()      { return "used".equalsIgnoreCase(tab); }
}

