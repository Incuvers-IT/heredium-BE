package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.SupervisorPermission;
import art.heredium.domain.docent.model.dto.request.GetAdminDocentRequest;
import art.heredium.domain.docent.model.dto.request.PostAdminDocentRequest;
import art.heredium.service.DocentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/docents")
public class AdminDocentController {

  private final DocentService docentService;

  @GetMapping
  @SupervisorPermission
  public ResponseEntity list(@Valid GetAdminDocentRequest dto, Pageable pageable) {
    return ResponseEntity.ok(docentService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  @SupervisorPermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(docentService.detailByAdmin(id));
  }

  @GetMapping("/{id}/infos")
  @SupervisorPermission
  public ResponseEntity detailInfos(@PathVariable Long id, Pageable pageable) {
    return ResponseEntity.ok(docentService.infos(id, pageable));
  }

  @PostMapping
  @SupervisorPermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminDocentRequest dto) {
    return ResponseEntity.ok(docentService.insert(dto));
  }

  @PutMapping("/{id}")
  @SupervisorPermission
  public ResponseEntity update(
      @PathVariable Long id, @RequestBody @Valid PostAdminDocentRequest dto) {
    return ResponseEntity.ok(docentService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @SupervisorPermission
  public ResponseEntity delete(@PathVariable Long id) {
    return ResponseEntity.ok(docentService.delete(id));
  }
}
