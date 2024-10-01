package art.heredium.domain.holiday.model.dto.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetHolidayRequest {
  private LocalDate startDate;
  private LocalDate endDate;
  private Integer year;
}
