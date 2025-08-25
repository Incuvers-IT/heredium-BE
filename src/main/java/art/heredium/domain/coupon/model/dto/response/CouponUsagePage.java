package art.heredium.domain.coupon.model.dto.response;

import lombok.Getter; import lombok.Setter;
import java.util.List;

@Getter @Setter
public class CouponUsagePage {
    private List<CouponResponseDto> content;
    private long totalElements;

    private long totalCoupons;
    private long expiringCoupons;

    private int number;   // 0-based
    private int size;
    private int totalPages;
    private boolean first;
    private boolean last;
}
