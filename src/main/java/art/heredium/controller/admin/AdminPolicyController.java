package art.heredium.controller.admin;

import art.heredium.domain.policy.model.dto.request.GetAdminPolicyRequest;
import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.policy.model.dto.request.GetAdminPolicyPostCheckRequest;
import art.heredium.domain.policy.model.dto.request.PostAdminPolicyRequest;
import art.heredium.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/policies")
public class AdminPolicyController {

    private final PolicyService policyService;

    @GetMapping
    @ManagerPermission
    public ResponseEntity list(@Valid GetAdminPolicyRequest dto, Pageable pageable) {
        return ResponseEntity.ok(policyService.list(dto, pageable));
    }

    @GetMapping("/post-check")
    @ManagerPermission
    public ResponseEntity postCheck(@Valid GetAdminPolicyPostCheckRequest dto) {
        return ResponseEntity.ok(policyService.postCheck(dto));
    }

    @GetMapping("/{id}")
    @ManagerPermission
    public ResponseEntity detail(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.detail(id));
    }

    @PostMapping
    @ManagerPermission
    public ResponseEntity insert(@RequestBody @Valid PostAdminPolicyRequest dto) {
        return ResponseEntity.ok(policyService.insert(dto));
    }

    @PutMapping("/{id}")
    @ManagerPermission
    public ResponseEntity update(@PathVariable Long id, @RequestBody @Valid PostAdminPolicyRequest dto) {
        return ResponseEntity.ok(policyService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ManagerPermission
    public ResponseEntity delete(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.delete(id));
    }
}