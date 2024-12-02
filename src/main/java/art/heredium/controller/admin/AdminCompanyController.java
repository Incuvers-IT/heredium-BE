package art.heredium.controller.admin;

import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.core.util.ValidationUtil;
import art.heredium.domain.company.model.dto.request.CompanyCreateRequest;
import art.heredium.domain.company.model.dto.response.CompanyMembershipRegistrationResponse;
import art.heredium.domain.company.model.dto.response.CompanyResponseDto;
import art.heredium.service.CompanyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/companies")
@ManagerPermission
public class AdminCompanyController {
  private final CompanyService companyService;

  @PostMapping
  public ResponseEntity<Long> createCompany(@RequestBody final CompanyCreateRequest request) {
    return ResponseEntity.ok(this.companyService.createCompany(request));
  }

  @GetMapping
  public ResponseEntity<List<CompanyResponseDto>> getCompanyList() {
    return ResponseEntity.ok(this.companyService.getAllCompanies());
  }

  @PostMapping("/{companyId}/membership-registrations/upload")
  public synchronized ResponseEntity<CompanyMembershipRegistrationResponse>
      uploadMembershipRegistrations(
          @PathVariable Long companyId, @RequestParam("file") MultipartFile file)
          throws IOException {
    ValidationUtil.validateExcelExtension(file);
    CompanyMembershipRegistrationResponse response =
        companyService.uploadMembershipRegistration(companyId, file);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/membership-registrations/validate")
  public ResponseEntity<?> validateMembershipRegistrations(
      @RequestParam("file") MultipartFile file) {
    ValidationUtil.validateExcelExtension(file);
    List<String> existingMemberships = companyService.getExistingMembershipRegistration(file);
    if (existingMemberships.isEmpty()) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.badRequest().body(existingMemberships);
  }
}
