package art.heredium.controller.user;

import art.heredium.domain.exhibition.model.dto.request.GetUserExhibitionRequest;
import art.heredium.service.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/exhibitions")
public class UserExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    public ResponseEntity list(@Valid GetUserExhibitionRequest dto, Pageable pageable) {
        return ResponseEntity.ok(exhibitionService.list(dto, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity detail(@PathVariable Long id) {
        return ResponseEntity.ok(exhibitionService.detailByUser(id));
    }

    @GetMapping("/{id}/rounds")
    public ResponseEntity rounds(@PathVariable Long id) {
        return ResponseEntity.ok(exhibitionService.detailRound(id));
    }

    @GetMapping("/{id}/rounds/info")
    public ResponseEntity rounds(@PathVariable Long id, @RequestParam("date") LocalDate date, @RequestParam(required = false) String encodeData) {
        return ResponseEntity.ok(exhibitionService.detailRoundInfo(id, date, encodeData));
    }
}