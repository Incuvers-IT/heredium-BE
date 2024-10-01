package art.heredium.domain.ticket.helper.chain;

public abstract class AbstractRoundValidator {
  private AbstractRoundValidator next;

  private boolean hasNext() {
    return this.next != null;
  }

  protected void next() {
    if (hasNext()) {
      this.next.run();
    }
  }

  public void chain(AbstractRoundValidator toChain) {
    if (this.next == null) {
      this.next = toChain;
    } else {
      this.next.chain(toChain);
    }
  }

  protected abstract void validate();

  public void run() {
    validate();
    next();
  }
}
