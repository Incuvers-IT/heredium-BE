package art.heredium.controller.user;

import art.heredium.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/events")
public class UserEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity list(Pageable pageable) {
        return ResponseEntity.ok(eventService.listByUser(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity detail(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.detailByUser(id));
    }
}