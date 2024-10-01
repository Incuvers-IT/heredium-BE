package art.heredium.ncloud.error;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum NCloudKitErrorCode {
  NONE(HttpStatus.OK),

  SDK_ERROR(HttpStatus.BAD_REQUEST),

  SMS_TYPE_INVALID(HttpStatus.BAD_REQUEST),
  SMS_FILE_LENGTH(HttpStatus.BAD_REQUEST),
  SMS_TO_INVALID(HttpStatus.BAD_REQUEST),
  SMS_CONTENT_INVALID(HttpStatus.BAD_REQUEST),

  BIZ_TEMPLATE_INVALID(HttpStatus.BAD_REQUEST),
  BIZ_FAILOVER_INAVLID(HttpStatus.BAD_REQUEST),

  CREDENTIAL_TIMSTAMP_INVALID(HttpStatus.BAD_REQUEST),
  CREDENTIAL_METHOD_INAVLID(HttpStatus.BAD_REQUEST),
  CREDENTIAL_URL_INVALID(HttpStatus.BAD_REQUEST);
  private final HttpStatus status;

  NCloudKitErrorCode(final HttpStatus status) {
    this.status = status;
  }
}
