package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.notice.model.dto.request.GetAdminNoticeRequest;
import art.heredium.domain.notice.model.dto.request.PostAdminNoticeRequest;
import art.heredium.service.NoticeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

  private final NoticeService noticeService;

  @GetMapping
  @ManagerPermission
  public ResponseEntity list(@Valid GetAdminNoticeRequest dto, Pageable pageable) {
    return ResponseEntity.ok(noticeService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  @ManagerPermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(noticeService.detailByAdmin(id));
  }

  @PostMapping
  @ManagerPermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminNoticeRequest dto) {
    return ResponseEntity.ok(noticeService.insert(dto));
  }

  @PutMapping("/{id}")
  @ManagerPermission
  public ResponseEntity update(
      @PathVariable Long id, @RequestBody @Valid PostAdminNoticeRequest dto) {
    return ResponseEntity.ok(noticeService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @ManagerPermission
  public ResponseEntity delete(@PathVariable Long id) {
    return ResponseEntity.ok(noticeService.delete(id));
  }
}
