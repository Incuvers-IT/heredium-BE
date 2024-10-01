package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.event.model.dto.request.GetAdminEventRequest;
import art.heredium.domain.event.model.dto.request.PostAdminEventRequest;
import art.heredium.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
public class AdminEventController {

  private final EventService eventService;

  @GetMapping
  @ManagerPermission
  public ResponseEntity list(@Valid GetAdminEventRequest dto, Pageable pageable) {
    return ResponseEntity.ok(eventService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  @ManagerPermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(eventService.detailByAdmin(id));
  }

  @PostMapping
  @ManagerPermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminEventRequest dto) {
    return ResponseEntity.ok(eventService.insert(dto));
  }

  @PutMapping("/{id}")
  @ManagerPermission
  public ResponseEntity update(
      @PathVariable Long id, @RequestBody @Valid PostAdminEventRequest dto) {
    return ResponseEntity.ok(eventService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @ManagerPermission
  public ResponseEntity delete(@PathVariable Long id) {
    return ResponseEntity.ok(eventService.delete(id));
  }
}
