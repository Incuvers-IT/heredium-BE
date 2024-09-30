package art.heredium.controller.user;

import art.heredium.domain.coffee.model.dto.request.GetUserCoffeeRequest;
import art.heredium.service.CoffeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/coffees")
public class UserCoffeeController {

    private final CoffeeService coffeeService;

    @GetMapping
    public ResponseEntity list(@Valid GetUserCoffeeRequest dto, Pageable pageable) {
        return ResponseEntity.ok(coffeeService.list(dto, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity detail(@PathVariable Long id) {
        return ResponseEntity.ok(coffeeService.detailByUser(id));
    }

    @GetMapping("/{id}/rounds")
    public ResponseEntity rounds(@PathVariable Long id) {
        return ResponseEntity.ok(coffeeService.detailRound(id));
    }

    @GetMapping("/{id}/rounds/info")
    public ResponseEntity rounds(@PathVariable Long id, @RequestParam("date") LocalDate date, @RequestParam(required = false) String encodeData) {
        return ResponseEntity.ok(coffeeService.detailRoundInfo(id, date, encodeData));
    }
}