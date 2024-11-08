package art.heredium.core.config.error;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.DeletedMembershipException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.error.entity.ErrorEntity;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * javax.validation.Valid or @Validated 으로 binding error 발생시 발생한다. HttpMessageConverter 에서 등록한
   * HttpMessageConverter binding 못할경우 발생 주로 @RequestBody, @RequestPart 어노테이션에서 발생
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.error("handleMethodArgumentNotValidException", e);
    BindingResult bindingResult = e.getBindingResult();
    FieldError fieldError = bindingResult.getFieldErrors().get(0);
    return ErrorEntity.status(ErrorCode.BAD_VALID)
        .body(fieldError.getField() + fieldError.getDefaultMessage());
  }

  /**
   * @ModelAttribut 으로 binding error 발생시 BindException 발생한다. ref
   * https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-modelattrib-method-args
   */
  @ExceptionHandler(BindException.class)
  protected ResponseEntity handleBindException(BindException e) {
    log.error("handleBindException", e);
    BindingResult bindingResult = e.getBindingResult();
    FieldError fieldError = bindingResult.getFieldErrors().get(0);
    return ErrorEntity.status(ErrorCode.BAD_VALID)
        .body(fieldError.getField() + fieldError.getDefaultMessage());
  }

  /** enum type 일치하지 않아 binding 못할 경우 발생 주로 @RequestParam enum으로 binding 못했을 경우 발생 */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  protected ResponseEntity handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    log.error("handleMethodArgumentTypeMismatchException", e);
    return ErrorEntity.status(ErrorCode.BAD_REQUEST).body(e.getMessage());
  }

  /** 지원하지 않은 HTTP method 호출 할 경우 발생 */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    log.error("handleHttpRequestMethodNotSupportedException", e);
    return ErrorEntity.status(ErrorCode.METHOD_NOT_ALLOWED).body(e.getMessage());
  }

  /** Authentication 객체가 필요한 권한을 보유하지 않은 경우 발생합 */
  @ExceptionHandler(AccessDeniedException.class)
  protected ResponseEntity handleAccessDeniedException(AccessDeniedException e) {
    return ErrorEntity.status(ErrorCode.FORBIDDEN).body(e.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  protected ResponseEntity handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    return ErrorEntity.status(ErrorCode.MAX_UPLOAD_SIZE).body(null);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity handleRuntimeException(RuntimeException e) {
    log.error("handleRuntimeException", e);
    return ErrorEntity.status(ErrorCode.INTERNAL_SERVER_ERROR).body(null);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity handleAuthenticationException(AuthenticationException e) {
    return ErrorEntity.status(ErrorCode.UNAUTHORIZED).body(e.getMessage());
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity handleUsernameNotFoundException(AuthenticationException e) {
    return ErrorEntity.status(ErrorCode.TOKEN_USER_NOT_FOUND).body(e.getMessage());
  }

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity handleDisabledException(AuthenticationException e) {
    return ErrorEntity.status(ErrorCode.USER_DISABLED).body(e.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    log.error("handleHttpMessageNotReadableException", e);
    return ErrorEntity.status(ErrorCode.JSON_PARSE_ERROR).body(null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ResponseEntity handleDataIntegrityViolationException(
      DataIntegrityViolationException e) {
    String message = e.getMostSpecificCause().getMessage();
    if (message.startsWith("Duplicate entry")) {
      return ErrorEntity.status(ErrorCode.DUPLICATE_KEY).body(null);
    }
    if (message.startsWith("Cannot delete or update a parent row")) {
      return ErrorEntity.status(ErrorCode.EXIST_PARENT).body(null);
    }
    log.error("DataIntegrityViolationException", e);
    return ErrorEntity.status(ErrorCode.INTERNAL_SERVER_ERROR).body(null);
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity handleGlobalException(Exception e) {
    log.error("handleGlobalException", e);
    return ErrorEntity.status(ErrorCode.INTERNAL_SERVER_ERROR).body(null);
  }

  @ExceptionHandler(ApiException.class)
  protected ResponseEntity handleApiException(ApiException e) {
    log.error("ApiException", e);
    return ErrorEntity.status(e.getErrorCode()).state(e.getState()).body(e.getBody());
  }

  @ExceptionHandler(DeletedMembershipException.class)
  protected ResponseEntity handleDeletedMembershipException(DeletedMembershipException e) {
    log.error("DeletedMembershipException", e);
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("MESSAGE", e.getBody());
    return ResponseEntity.status(ErrorCode.MEMBERSHIP_NOT_FOUND.getStatus()).body(errorResponse);
  }
}
