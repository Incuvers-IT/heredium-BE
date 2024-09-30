package art.heredium.controller.admin;

import art.heredium.core.annotation.SupervisorPermission;
import art.heredium.domain.account.model.dto.request.*;
import art.heredium.service.HanaBankUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/hana-bank")
public class AdminHanaBankUserController {

    private final HanaBankUserService hanaBankUserService;

    @GetMapping
    @SupervisorPermission
    public ResponseEntity list(@Valid GetAdminHanaBankRequest dto, Pageable pageable) {
        return ResponseEntity.ok(hanaBankUserService.list(dto, pageable));
    }

    @GetMapping("/{id}")
    @SupervisorPermission
    public ResponseEntity detail(@PathVariable Long id) {
        return ResponseEntity.ok(hanaBankUserService.detailByAdmin(id));
    }


    @GetMapping("/{id}/tickets")
    @SupervisorPermission
    public ResponseEntity tickets(@PathVariable Long id, @RequestParam("isCoffee") Boolean isCoffee, Pageable pageable) {
        return ResponseEntity.ok(hanaBankUserService.ticketByHanaBankUser(id, isCoffee, pageable));
    }
}