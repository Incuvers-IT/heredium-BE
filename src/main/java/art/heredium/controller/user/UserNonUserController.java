package art.heredium.controller.user;

import art.heredium.domain.account.model.dto.request.GetUserNonUserTicketRequest;
import art.heredium.service.TicketNonUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/non-user")
public class UserNonUserController {

    private final TicketNonUserService ticketNonUserService;

    @PostMapping("/tickets")
    public ResponseEntity list(@RequestBody @Valid GetUserNonUserTicketRequest dto, Pageable pageable) {
        return ResponseEntity.ok(ticketNonUserService.list(dto, pageable));
    }

    @PostMapping("/tickets/{id}")
    public ResponseEntity detail(@PathVariable Long id, @RequestBody @Valid GetUserNonUserTicketRequest dto) {
        return ResponseEntity.ok(ticketNonUserService.detail(id, dto));
    }

    @PutMapping("/tickets/{id}/refund")
    public ResponseEntity ticketRefund(@PathVariable Long id, @RequestBody @Valid GetUserNonUserTicketRequest dto) {
        return ResponseEntity.ok(ticketNonUserService.refund(id, dto));
    }
}