package art.heredium.controller.user;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.service.NoticeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/notices")
public class UserNoticeController {

  private final NoticeService noticeService;

  @GetMapping
  public ResponseEntity list(Pageable pageable) {
    return ResponseEntity.ok(noticeService.listByUser(pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(noticeService.detailByUser(id));
  }
}
