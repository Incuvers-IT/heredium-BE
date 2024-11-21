package art.heredium.domain.ticket.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketMembershipRegistrationInfo {
  private Long id;
  private String name;
  private LocalDateTime registrationDate;
  private LocalDateTime expirationDate;
}
