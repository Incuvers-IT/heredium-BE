package art.heredium.core.config.error.entity;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  // 커스텀  에러
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
  MAX_UPLOAD_SIZE(HttpStatus.INTERNAL_SERVER_ERROR),
  HANDLE_ACCESS_DENIED(HttpStatus.INTERNAL_SERVER_ERROR),
  DATA_NOT_FOUND(HttpStatus.BAD_REQUEST),
  DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
  MAIL(HttpStatus.INTERNAL_SERVER_ERROR),

  // 4XX 공통 에러
  BAD_REQUEST(HttpStatus.BAD_REQUEST),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
  JSON_PARSE_ERROR(HttpStatus.UNAUTHORIZED),
  BAD_VALID(HttpStatus.BAD_REQUEST),
  FORBIDDEN(HttpStatus.FORBIDDEN),
  NOT_FOUND(HttpStatus.NOT_FOUND),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
  REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT),
  LENGTH_REQUIRED(HttpStatus.REQUEST_TIMEOUT),
  PRECONDITION_FAILED(HttpStatus.PRECONDITION_FAILED),
  PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE),
  UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
  DUPLICATE_KEY(HttpStatus.BAD_REQUEST),
  EXIST_PARENT(HttpStatus.BAD_REQUEST),

  TOKEN_VALID_FAIL(HttpStatus.BAD_REQUEST),
  TOKEN_USER_NOT_FOUND(HttpStatus.BAD_REQUEST),

  USER_NOT_FOUND(HttpStatus.BAD_REQUEST),
  PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST),
  ALREADY_EXIST_USERNAME(HttpStatus.BAD_REQUEST),
  FIRST_ADMIN_AUTHORITY(HttpStatus.BAD_REQUEST),
  FIRST_ADMIN_DELETE(HttpStatus.BAD_REQUEST),
  LOGIN_FAIL_OVER(HttpStatus.BAD_REQUEST),
  NEED_CHANGE_PASSWORD(HttpStatus.BAD_REQUEST),
  VERIFY_TIMEOUT(HttpStatus.BAD_REQUEST),
  VERIFY_NOT_MATCHED(HttpStatus.BAD_REQUEST),
  PASSWORD_EQUAL(HttpStatus.BAD_REQUEST),
  USER_DISABLED(HttpStatus.BAD_REQUEST),
  USER_SLEEPER(HttpStatus.BAD_REQUEST),
  USER_NOT_SLEEPER(HttpStatus.BAD_REQUEST),
  DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST),

  INVALID_FILE(HttpStatus.BAD_REQUEST),

  SOLD_OUT(HttpStatus.BAD_REQUEST),
  TICKET_REFUND(HttpStatus.BAD_REQUEST),
  TICKET_EXPIRED(HttpStatus.BAD_REQUEST),
  TICKET_USED(HttpStatus.BAD_REQUEST),
  NOT_OPEN(HttpStatus.BAD_REQUEST),
  THE_END(HttpStatus.BAD_REQUEST),
  TICKET_NOT_ALLOW(HttpStatus.BAD_REQUEST),
  S3_NOT_FOUND(HttpStatus.BAD_REQUEST),
  NOT_EQ_PHONE(HttpStatus.BAD_REQUEST),
  UNDER_FOURTEEN(HttpStatus.BAD_REQUEST),
  TIMEOUT(HttpStatus.BAD_REQUEST),
  MEMBERSHIP_REGISTRATION_NOT_FOUND(HttpStatus.NOT_FOUND),
  MEMBERSHIP_REGISTRATION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST),
  REGISTERING_MEMBERSHIP_IS_NOT_AVAILABLE(HttpStatus.BAD_REQUEST),
  MEMBERSHIP_NOT_FOUND(HttpStatus.NOT_FOUND),
  POST_NOT_FOUND(HttpStatus.NOT_FOUND),
  POST_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND),
  INVALID_POST_STATUS_TO_ENABLE_MEMBERSHIP(HttpStatus.BAD_REQUEST),
  ANONYMOUS_USER(HttpStatus.FORBIDDEN),
  POST_NOT_ALLOW(HttpStatus.BAD_REQUEST),

  COUPON_NOT_FOUND(HttpStatus.NOT_FOUND),
  COUPON_USAGE_NOT_FOUND(HttpStatus.NOT_FOUND),
  COUPON_EXPIRED(HttpStatus.BAD_REQUEST),
  COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST),
  INVALID_COUPON_TO_ASSIGN(HttpStatus.BAD_REQUEST),

  ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND),

  TICKET_PRICE_NOT_FOUND(HttpStatus.NOT_FOUND),
  COUPON_NOT_APPLICABLE(HttpStatus.BAD_REQUEST),
  COUPON_ALREADY_IN_USE(HttpStatus.BAD_REQUEST),
  COMPANY_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST),
  COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND),
  INVALID_COUPON_SOURCE(HttpStatus.BAD_REQUEST),
  INVALID_COUPON_PERIOD(HttpStatus.BAD_REQUEST),
  PAYMENT_ORDER_ID_NOT_FOUND(HttpStatus.NOT_FOUND),
  INVALID_EXCEL_FILE(HttpStatus.BAD_REQUEST),
  INVALID_EXCEL_COLUMNS(HttpStatus.BAD_REQUEST),
  POST_ALREADY_EXISTED(HttpStatus.BAD_REQUEST),
  INVALID_REGISTRATION_DATE(HttpStatus.BAD_REQUEST),
  INVALID_MEMBERSHIP_TO_REFUND(HttpStatus.BAD_REQUEST),
  COUPON_NOT_ALLOWED(HttpStatus.BAD_REQUEST);
  private HttpStatus status;

  ErrorCode(final HttpStatus status) {
    this.status = status;
  }
}
