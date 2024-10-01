package art.heredium.controller.user;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.service.DocentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/docents")
public class UserDocentController {

  private final DocentService docentService;

  @GetMapping
  public ResponseEntity list() {
    return ResponseEntity.ok(docentService.list());
  }

  @GetMapping("/{id}/infos")
  public ResponseEntity detailInfos(@PathVariable Long id) {
    return ResponseEntity.ok(docentService.infos(id));
  }

  @GetMapping("/{id}/infos/{infoId}")
  public ResponseEntity detailInfos(@PathVariable Long id, @PathVariable Long infoId) {
    return ResponseEntity.ok(docentService.detailInfo(id, infoId));
  }
}
