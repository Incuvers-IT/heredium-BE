package art.heredium.domain.ticket.helper;

import art.heredium.domain.ticket.helper.chain.AbstractRoundValidator;

public class TicketRoundValidator {

  private AbstractRoundValidator head;

  public TicketRoundValidator chain(AbstractRoundValidator validator) {
    if (this.head == null) this.head = validator;
    else this.head.chain(validator);
    return this;
  }

  public void validate() {
    this.head.run();
  }
}
