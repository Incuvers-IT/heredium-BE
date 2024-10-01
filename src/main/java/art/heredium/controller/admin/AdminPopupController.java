package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.popup.model.dto.request.GetAdminPopupRequest;
import art.heredium.domain.popup.model.dto.request.PostAdminPopupRequest;
import art.heredium.domain.popup.model.dto.request.PutAdminPopupOrderRequest;
import art.heredium.service.PopupService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/popups")
public class AdminPopupController {

  private final PopupService popupService;

  @GetMapping
  @ManagerPermission
  public ResponseEntity list(@Valid GetAdminPopupRequest dto, Pageable pageable) {
    return ResponseEntity.ok(popupService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  @ManagerPermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(popupService.detailByAdmin(id));
  }

  @PostMapping
  @ManagerPermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminPopupRequest dto) {
    return ResponseEntity.ok(popupService.insert(dto));
  }

  @PutMapping("/{id}")
  @ManagerPermission
  public ResponseEntity update(
      @PathVariable Long id, @RequestBody @Valid PostAdminPopupRequest dto) {
    return ResponseEntity.ok(popupService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @ManagerPermission
  public ResponseEntity delete(@PathVariable Long id) {
    return ResponseEntity.ok(popupService.delete(id));
  }

  @PutMapping("/order")
  @ManagerPermission
  public ResponseEntity updateOrder(@Valid PutAdminPopupOrderRequest dto) {
    return ResponseEntity.ok(popupService.updateOrder(dto));
  }

  @PutMapping("/{id}/enabled")
  @ManagerPermission
  public ResponseEntity updateEnabled(
      @PathVariable Long id, @RequestParam("isEnabled") boolean isEnabled) {
    return ResponseEntity.ok(popupService.updateEnabled(id, isEnabled));
  }
}
