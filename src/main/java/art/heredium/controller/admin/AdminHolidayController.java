package art.heredium.controller.admin;

import art.heredium.core.annotation.CoffeePermission;
import art.heredium.core.annotation.SupervisorPermission;
import art.heredium.domain.holiday.model.dto.request.GetHolidayRequest;
import art.heredium.domain.holiday.model.dto.request.PostHolidayRequest;
import art.heredium.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/holidays")
public class AdminHolidayController {

    private final HolidayService holidayService;

    @GetMapping
    @CoffeePermission
    public ResponseEntity list(GetHolidayRequest dto) {
        return ResponseEntity.ok(holidayService.list(dto));
    }

    @GetMapping("/days")
    @CoffeePermission
    public ResponseEntity days() {
        return ResponseEntity.ok(holidayService.getDays());
    }

    @GetMapping("/last-start-date")
    @CoffeePermission
    public ResponseEntity getLastStartDate() {
        return ResponseEntity.ok(holidayService.getLastStartDate());
    }

    @PostMapping
    @SupervisorPermission
    public ResponseEntity insert(@RequestBody @Valid PostHolidayRequest dto) {
        return ResponseEntity.ok(holidayService.insert(dto));
    }
}