package art.heredium.domain.holiday.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GetHolidayRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer year;
}
