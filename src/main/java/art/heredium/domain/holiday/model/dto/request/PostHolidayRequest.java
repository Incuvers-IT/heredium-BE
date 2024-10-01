package art.heredium.domain.holiday.model.dto.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostHolidayRequest {
  private List<Integer> days = new ArrayList<>();
  private List<LocalDate> insert = new ArrayList<>();
  private List<LocalDate> delete = new ArrayList<>();
}
