package art.heredium.controller.user;

import art.heredium.domain.ticket.model.TicketOrderInfo;
import art.heredium.domain.ticket.model.dto.request.PostTicketUserRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketUserValidRequest;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;
import art.heredium.service.TicketUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 회원 예매
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/tickets/user")
public class UserTicketUserController {

    private final TicketUserService ticketUserService;

    @PostMapping("/valid")
    public ResponseEntity valid(@RequestBody @Valid PostTicketUserValidRequest ticketOrderInfo) {
        return ResponseEntity.ok(ticketUserService.valid(ticketOrderInfo));
    }

    @PostMapping
    public ResponseEntity insert(@RequestBody @Valid PostTicketUserRequest dto) {
        return ResponseEntity.ok(ticketUserService.insert(dto));
    }

    @PostMapping("/free")
    public ResponseEntity insert(@RequestBody @Valid PostTicketUserValidRequest ticketOrderInfo) {
        return ResponseEntity.ok(ticketUserService.insert(ticketOrderInfo));
    }
}