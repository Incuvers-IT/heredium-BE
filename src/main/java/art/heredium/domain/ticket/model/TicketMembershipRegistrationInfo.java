package art.heredium.domain.ticket.model;

import java.time.LocalDate;

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
  private LocalDate registrationDate;
  private LocalDate expirationDate;
}
