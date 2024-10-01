package art.heredium.domain.holiday.component;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.holiday.repository.HolidayRepository;

@Component
@Transactional(readOnly = true)
public class HolidayManager {

  private HolidayRepository holidayRepository;
  private static HolidayRepository staticHolidayRepository;

  public HolidayManager(HolidayRepository holidayRepository) {
    this.holidayRepository = holidayRepository;
  }

  @PostConstruct
  public void postConstruct() {
    staticHolidayRepository = holidayRepository;
  }

  public static boolean isHoliday(LocalDate atDay) {
    return staticHolidayRepository.existsByDay(atDay);
  }
}
