package art.heredium.domain.membership.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MembershipMileagePage {
    private List<MembershipMileageResponse> content;
    private long totalElements;
    private long totalMileage;
    private long expiringMileage;
    private int number;
    private int size;
    private int totalPages;
    private boolean first;
    private boolean last;
}
