package art.heredium.controller.admin;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.company.model.dto.request.CompanyCreateRequest;
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
}
