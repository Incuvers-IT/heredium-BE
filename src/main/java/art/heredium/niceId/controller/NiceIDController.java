package art.heredium.niceId.controller;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.niceId.model.dto.request.PostNiceIdEncryptRequest;
import art.heredium.niceId.service.NiceIdService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/nice")
public class NiceIDController {

  private final NiceIdService niceIdService;

  @GetMapping("/encrypt")
  public ResponseEntity encrypt(PostNiceIdEncryptRequest dto) {
    return ResponseEntity.ok(niceIdService.encrypt(dto));
  }

  @GetMapping("/decrypt")
  public ResponseEntity decrypt(@RequestParam("encodeData") String encodeData) {
    return ResponseEntity.ok(niceIdService.decrypt(encodeData));
  }
}
