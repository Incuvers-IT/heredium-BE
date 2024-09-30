package art.heredium.domain.holiday.repository;

import art.heredium.domain.holiday.entity.HolidayInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayInfoRepository extends JpaRepository<HolidayInfo, Long> {
    HolidayInfo findTop1ByOrderById();
}
