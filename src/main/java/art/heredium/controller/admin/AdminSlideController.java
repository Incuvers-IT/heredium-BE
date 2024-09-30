package art.heredium.controller.admin;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.slide.model.dto.request.GetAdminSlideRequest;
import art.heredium.domain.slide.model.dto.request.PostAdminSlideRequest;
import art.heredium.domain.slide.model.dto.request.PutAdminSlideOrderRequest;
import art.heredium.service.SlideService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/slides")
public class AdminSlideController {

    private final SlideService slideService;

    @GetMapping
    @ManagerPermission
    public ResponseEntity list(@Valid GetAdminSlideRequest dto, Pageable pageable) {
        return ResponseEntity.ok(slideService.list(dto, pageable));
    }

    @GetMapping("/{id}")
    @ManagerPermission
    public ResponseEntity detail(@PathVariable Long id) {
        return ResponseEntity.ok(slideService.detailByAdmin(id));
    }

    @PostMapping
    @ManagerPermission
    public ResponseEntity insert(@RequestBody @Valid PostAdminSlideRequest dto) {
        return ResponseEntity.ok(slideService.insert(dto));
    }

    @PutMapping("/{id}")
    @ManagerPermission
    public ResponseEntity update(@PathVariable Long id, @RequestBody @Valid PostAdminSlideRequest dto) {
        return ResponseEntity.ok(slideService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ManagerPermission
    public ResponseEntity delete(@PathVariable Long id) {
        return ResponseEntity.ok(slideService.delete(id));
    }

    @PutMapping("/order")
    @ManagerPermission
    public ResponseEntity updateOrder(@Valid PutAdminSlideOrderRequest dto) {
        return ResponseEntity.ok(slideService.updateOrder(dto));
    }

    @PutMapping("/{id}/enabled")
    @ManagerPermission
    public ResponseEntity updateEnabled(@PathVariable Long id, @RequestParam("isEnabled") boolean isEnabled) {
        return ResponseEntity.ok(slideService.updateEnabled(id, isEnabled));
    }
}