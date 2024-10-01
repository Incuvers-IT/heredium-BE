package art.heredium;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.model.dto.request.PostAdminRequest;
import art.heredium.domain.account.repository.AdminRepository;
import art.heredium.domain.account.type.AuthType;
import art.heredium.domain.holiday.entity.HolidayInfo;
import art.heredium.domain.holiday.repository.HolidayInfoRepository;
import art.heredium.service.AdminService;

@Component
@RequiredArgsConstructor
public class InitDb {

  private final InitService initService;

  @PostConstruct
  public void init() {
    initService.dbInit();
  }

  @Component
  @Transactional
  @RequiredArgsConstructor
  static class InitService {

    private final HolidayInfoRepository holidayInfoRepository;
    private final AdminRepository adminRepository;
    private final AdminService adminService;

    public void dbInit() {
      Admin firstAccount = adminRepository.findTop1ByOrderById();
      if (firstAccount == null) {
        PostAdminRequest dto = new PostAdminRequest();
        dto.setEmail("jang@spadecompany.kr");
        dto.setPassword("Spa39030!!");
        dto.setPhone("01012345678");
        dto.setName("관리자");
        dto.setAuth(AuthType.ADMIN);
        adminService.insert(dto, true);
      }
      HolidayInfo firstHolidayInfo = holidayInfoRepository.findTop1ByOrderById();
      if (firstHolidayInfo == null) {
        HolidayInfo entity = new HolidayInfo(new ArrayList<>());
        holidayInfoRepository.save(entity);
      }
    }
  }
}
