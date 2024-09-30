package art.heredium.domain.holiday.repository;

import art.heredium.domain.holiday.entity.Holiday;
import art.heredium.domain.holiday.model.dto.request.GetHolidayRequest;

import java.util.List;

public interface HolidayRepositoryQueryDsl {
    List<Holiday> search(GetHolidayRequest dto);
}
