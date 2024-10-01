package art.heredium.domain.holiday.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.holiday.entity.HolidayInfo;

public interface HolidayInfoRepository extends JpaRepository<HolidayInfo, Long> {
  HolidayInfo findTop1ByOrderById();
}
