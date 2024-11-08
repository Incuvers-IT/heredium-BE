package art.heredium.core.config.error.entity;

public class DeletedMembershipException extends ApiException {
  public DeletedMembershipException(String message) {
    super(ErrorCode.MEMBERSHIP_NOT_FOUND, message);
  }
}
