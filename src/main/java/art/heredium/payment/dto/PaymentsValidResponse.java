package art.heredium.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.entity.Ticket;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PaymentsValidResponse {
  private String uuid;
  private String title;
  private Long amount;
  private String name;
  private String email;

  public static PaymentsValidResponse from(Ticket ticket) {
    return PaymentsValidResponse.builder()
        .uuid(ticket.getUuid())
        .title(ticket.getTitle())
        .amount(ticket.getPrice())
        .name(ticket.getName())
        .email(ticket.getEmail())
        .build();
  }
}
