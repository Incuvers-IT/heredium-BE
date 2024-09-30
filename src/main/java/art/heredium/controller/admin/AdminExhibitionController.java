package art.heredium.controller.admin;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.core.annotation.SupervisorPermission;
import art.heredium.domain.common.model.dto.response.Data;
import art.heredium.domain.exhibition.model.dto.request.GetAdminExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.request.PostAdminExhibitionRequest;
import art.heredium.service.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/exhibitions")
public class AdminExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    @ManagerPermission
    public ResponseEntity list(@Valid GetAdminExhibitionRequest dto, Pageable pageable) {
        return ResponseEntity.ok(exhibitionService.list(dto, pageable));
    }

    @GetMapping("/{id}")
    @ManagerPermission
    public ResponseEntity detail(@PathVariable Long id) {
        return ResponseEntity.ok(exhibitionService.detailByAdmin(id));
    }

    @PostMapping
    @SupervisorPermission
    public ResponseEntity insert(@RequestBody @Valid PostAdminExhibitionRequest dto) {
        return ResponseEntity.ok(exhibitionService.insert(dto));
    }

    @PutMapping("/{id}")
    @SupervisorPermission
    public ResponseEntity update(@PathVariable Long id, @RequestBody @Valid PostAdminExhibitionRequest dto) {
        return ResponseEntity.ok(exhibitionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @SupervisorPermission
    public ResponseEntity delete(@PathVariable Long id) {
        return ResponseEntity.ok(exhibitionService.delete(id));
    }

    @GetMapping("/expose/title")
    @ManagerPermission
    public ResponseEntity exposeTitle() {
        return ResponseEntity.ok(exhibitionService.exposeTitle());
    }

    @GetMapping("/{id}/rounds")
    @ManagerPermission
    public ResponseEntity detailRounds(@PathVariable Long id) {
        return ResponseEntity.ok(exhibitionService.detailRounds(id));
    }

    @PutMapping("/{id}/note")
    @SupervisorPermission
    public ResponseEntity updateNote(@PathVariable Long id, @RequestBody Data<String> data) {
        return ResponseEntity.ok(exhibitionService.updateNote(id, data));
    }
}