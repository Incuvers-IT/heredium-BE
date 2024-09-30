package art.heredium.controller.user;

import art.heredium.domain.ticket.model.dto.request.PostTicketNonUserRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketNonUserValidRequest;
import art.heredium.service.TicketNonUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 비회원 예매
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/tickets/non-user")
public class UserTicketNonUserController {

    private final TicketNonUserService ticketNonUserService;

    @PostMapping("/valid")
    public ResponseEntity valid(@RequestBody @Valid PostTicketNonUserValidRequest dto) {
        return ResponseEntity.ok(ticketNonUserService.valid(dto));
    }

    @PostMapping
    public ResponseEntity insert(@RequestBody @Valid PostTicketNonUserRequest dto) {
        return ResponseEntity.ok(ticketNonUserService.insert(dto));
    }

    @PostMapping("/free")
    public ResponseEntity insert(@RequestBody @Valid PostTicketNonUserValidRequest dto) {
        return ResponseEntity.ok(ticketNonUserService.insert(dto));
    }
}