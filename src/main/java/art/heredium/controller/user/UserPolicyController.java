package art.heredium.controller.user;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.domain.policy.type.PolicyType;
import art.heredium.service.PolicyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/policies")
public class UserPolicyController {

  private final PolicyService policyService;

  @GetMapping
  public ResponseEntity list(@RequestParam("type") PolicyType type) {
    return ResponseEntity.ok(policyService.listByUser(type));
  }

  @GetMapping("/{id}")
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(policyService.detailByUser(id));
  }

  @GetMapping("/posting")
  public ResponseEntity posting(@RequestParam("type") PolicyType type) {
    return ResponseEntity.ok(policyService.posting(type));
  }
}
