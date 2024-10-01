package art.heredium.domain.holiday.repository;

import java.util.List;

import art.heredium.domain.holiday.entity.Holiday;
import art.heredium.domain.holiday.model.dto.request.GetHolidayRequest;

public interface HolidayRepositoryQueryDsl {
  List<Holiday> search(GetHolidayRequest dto);
}
