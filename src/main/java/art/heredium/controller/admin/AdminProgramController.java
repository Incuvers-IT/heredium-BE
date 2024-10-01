package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.core.annotation.SupervisorPermission;
import art.heredium.domain.common.model.dto.response.Data;
import art.heredium.domain.program.model.dto.request.GetAdminProgramRequest;
import art.heredium.domain.program.model.dto.request.PostAdminProgramRequest;
import art.heredium.service.ProgramService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/programs")
public class AdminProgramController {

  private final ProgramService programService;

  @GetMapping
  @ManagerPermission
  public ResponseEntity list(@Valid GetAdminProgramRequest dto, Pageable pageable) {
    return ResponseEntity.ok(programService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  @ManagerPermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(programService.detailByAdmin(id));
  }

  @PostMapping
  @SupervisorPermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminProgramRequest dto) {
    return ResponseEntity.ok(programService.insert(dto));
  }

  @PutMapping("/{id}")
  @SupervisorPermission
  public ResponseEntity update(
      @PathVariable Long id, @RequestBody @Valid PostAdminProgramRequest dto) {
    return ResponseEntity.ok(programService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @SupervisorPermission
  public ResponseEntity delete(@PathVariable Long id) {
    return ResponseEntity.ok(programService.delete(id));
  }

  @GetMapping("/expose/title")
  @SupervisorPermission
  public ResponseEntity exposeTitle() {
    return ResponseEntity.ok(programService.exposeTitle());
  }

  @GetMapping("/{id}/rounds")
  @ManagerPermission
  public ResponseEntity detailRounds(@PathVariable Long id) {
    return ResponseEntity.ok(programService.detailRounds(id));
  }

  @PutMapping("/{id}/note")
  @SupervisorPermission
  public ResponseEntity updateNote(@PathVariable Long id, @RequestBody Data<String> data) {
    return ResponseEntity.ok(programService.updateNote(id, data));
  }
}
