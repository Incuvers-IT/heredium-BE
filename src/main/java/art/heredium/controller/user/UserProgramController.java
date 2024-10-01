package art.heredium.controller.user;

import java.time.LocalDate;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.domain.program.model.dto.request.GetUserProgramRequest;
import art.heredium.service.ProgramService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/programs")
public class UserProgramController {

  private final ProgramService programService;

  @GetMapping
  public ResponseEntity list(@Valid GetUserProgramRequest dto, Pageable pageable) {
    return ResponseEntity.ok(programService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(programService.detailByUser(id));
  }

  @GetMapping("/{id}/rounds")
  public ResponseEntity rounds(@PathVariable Long id) {
    return ResponseEntity.ok(programService.detailRound(id));
  }

  @GetMapping("/{id}/rounds/info")
  public ResponseEntity rounds(
      @PathVariable Long id,
      @RequestParam("date") LocalDate date,
      @RequestParam(required = false) String encodeData) {
    return ResponseEntity.ok(programService.detailRoundInfo(id, date, encodeData));
  }
}
