package art.heredium.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.util.Constants;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.exhibition.repository.ExhibitionRepository;
import art.heredium.domain.holiday.entity.Holiday;
import art.heredium.domain.holiday.entity.HolidayInfo;
import art.heredium.domain.holiday.model.dto.request.GetHolidayRequest;
import art.heredium.domain.holiday.model.dto.request.PostHolidayRequest;
import art.heredium.domain.holiday.repository.HolidayInfoRepository;
import art.heredium.domain.holiday.repository.HolidayRepository;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.program.repository.ProgramRepository;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class HolidayService {

  private final ExhibitionRepository exhibitionRepository;
  private final ProgramRepository programRepository;
  private final HolidayRepository holidayRepository;
  private final HolidayInfoRepository holidayInfoRepository;

  public List<LocalDate> list(GetHolidayRequest dto) {
    return holidayRepository.search(dto).stream().map(Holiday::getDay).collect(Collectors.toList());
  }

  public LocalDateTime getLastStartDate() {
    Exhibition exhibition = exhibitionRepository.findByLastStartDate();
    Program program = programRepository.findByLastStartDate();
    List<LocalDateTime> list = new ArrayList<>();
    list.add(Constants.getNow());
    if (exhibition != null) {
      list.add(exhibition.getBookingEndDate());
    }
    if (program != null) {
      list.add(program.getBookingEndDate());
    }
    return list.stream().filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
  }

  public List<Integer> getDays() {
    HolidayInfo holidayInfo = holidayInfoRepository.findTop1ByOrderById();
    return holidayInfo != null ? holidayInfo.getDays() : new ArrayList<>();
  }

  public boolean insert(PostHolidayRequest dto) {
    HolidayInfo holidayInfo = holidayInfoRepository.findTop1ByOrderById();
    if (holidayInfo == null) {
      holidayInfoRepository.save(new HolidayInfo(dto.getDays()));
    } else {
      holidayInfo.updateDays(dto.getDays());
    }
    dto.getInsert().forEach(holidayRepository::saveIgnore);
    holidayRepository.deleteAllByDayIn(dto.getDelete());
    return true;
  }
}
