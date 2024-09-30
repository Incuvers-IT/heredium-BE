package art.heredium.domain.statistics.model.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminStatisticsResponse {
    private Long visitNumber;
    private Long totalPrice;
    private Long signUp;

    public AdminStatisticsResponse(Long visitNumber, Long totalPrice, Long signUp) {
        this.visitNumber = visitNumber;
        this.totalPrice = totalPrice;
        this.signUp = signUp;
    }
}