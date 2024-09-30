package art.heredium.domain.holiday.component;

import art.heredium.domain.holiday.repository.HolidayRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class HolidayManager {

    private HolidayRepository holidayRepository;
    private static HolidayRepository staticHolidayRepository;

    public HolidayManager(HolidayRepository holidayRepository){
        this.holidayRepository = holidayRepository;
    }

    @PostConstruct
    public void postConstruct(){
        staticHolidayRepository = holidayRepository;
    }

    public static boolean isHoliday(LocalDate atDay){
        return staticHolidayRepository.existsByDay(atDay);
    }
}
