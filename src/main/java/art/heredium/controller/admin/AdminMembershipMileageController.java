package art.heredium.controller.admin;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.coffee.repository.CoffeeRepository;
import art.heredium.domain.exhibition.repository.ExhibitionRepository;
import art.heredium.domain.membership.entity.MembershipMileage;
import art.heredium.domain.membership.model.dto.request.*;
import art.heredium.domain.membership.model.dto.response.*;
import art.heredium.domain.program.repository.ProgramRepository;
import art.heredium.service.MembershipMileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/membershipMileage")
@ManagerPermission
public class AdminMembershipMileageController {

  private final MembershipMileageService membershipMileageService;
  private final ExhibitionRepository exhibitionRepo;
  private final ProgramRepository programRepo;
  private final CoffeeRepository coffeeRepo;

  @GetMapping("/{accountId}")
  public ResponseEntity<Page<MembershipMileageResponse>> getMembershipsMileageList(
          @PathVariable Long accountId,
          @Valid GetAllActiveMembershipsRequest request, Pageable pageable) {

    request.setAccountId(accountId);

    return ResponseEntity.ok(
            this.membershipMileageService.getMembershipsMileageList(request, pageable));
  }

  @GetMapping("/category/{category}")
  public List<TitleOptionDto> getTitlesByCategory(@PathVariable int category) {

    switch (category) {
      case 0: // 전시
      case 3: // 아트숍 시에도 전시로 연결
        return exhibitionRepo.findAllByProgress()
                .stream()
                .map(e -> new TitleOptionDto(e.getId(), e.getTitle()))
                .collect(Collectors.toList());

      case 1: // 프로그램
        return programRepo.findAllByProgress()
                .stream()
                .map(p -> new TitleOptionDto(p.getId(), p.getTitle()))
                .collect(Collectors.toList());

      case 2: // 커피
        return coffeeRepo.findAllByProgress()
                .stream()
                .map(c -> new TitleOptionDto(c.getId(), c.getTitle()))
                .collect(Collectors.toList());

      default:
        return Collections.emptyList();
    }
  }

  @PostMapping
  public ResponseEntity<Void> createMileage(@RequestBody MembershipMileageCreateRequest req) {
    membershipMileageService.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * 기존 마일리지(id) 를 찾아서 환불(type=3) 레코드를 복제 저장
   */
  @PostMapping("/{id}/refund")
  @ResponseStatus(HttpStatus.CREATED)
  public void refundMileage(@PathVariable Long id, @RequestBody RefundRequest request,
                            @RequestParam(value = "upgradeCancel", required = false, defaultValue = "false") boolean upgradeCancel) {
    membershipMileageService.refundMileage(id, request.getReason(), upgradeCancel);
  }

  @PostMapping("/cancel/check")
  public ResponseEntity<Map<String, Boolean>> checkCancel(
          @RequestBody @Valid CancelCheckRequest req
  ) {
    boolean canCancel = membershipMileageService.canCancelUpgrade(
            req.getAccountId(),
            req.getRelatedMileageId(),
            req.getMileageAmount()
    );
    Map<String, Boolean> result = new HashMap<>();
    result.put("canCancel", canCancel);
    return ResponseEntity.ok(result);
  }
}
