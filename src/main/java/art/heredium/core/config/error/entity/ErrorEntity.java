package art.heredium.core.config.error.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@Slf4j
public class ErrorEntity<T> extends ResponseEntity<T> {

  public ErrorEntity(HttpStatus status) {
    super(status);
  }

  public ErrorEntity(T body, HttpStatus status) {
    super(body, status);
  }

  public ErrorEntity(MultiValueMap<String, String> headers, HttpStatus status) {
    super(headers, status);
  }

  public ErrorEntity(T body, MultiValueMap<String, String> headers, HttpStatus status) {
    super(body, headers, status);
  }

  public ErrorEntity(T body, MultiValueMap<String, String> headers, int rawStatus) {
    super(body, headers, rawStatus);
  }

  public static EntityBuilder status(ErrorCode errorCode) {
    return new EntityBuilder(errorCode);
  }

  public static class EntityBuilder {
    private final ErrorCode errorCode;
    private Locale locale;
    private String message;
    private Integer state;

    public EntityBuilder(ErrorCode errorCode) {
      this.errorCode = errorCode;
    }

    public EntityBuilder locale(Locale locale) {
      this.locale = locale;
      return this;
    }

    public EntityBuilder state(Integer state) {
      this.state = state;
      return this;
    }

    public ResponseEntity body() {
      return body(null);
    }

    public ResponseEntity body(Object o) {
      Map<String, Object> object = new HashMap<>();
      object.put("MESSAGE", errorCode.name());
      object.put("HTTP_STATUS_CODE", errorCode.getStatus().value());
      object.put("HTTP_STATUS_MESSAGE", errorCode.getStatus());
      object.put("STATE", this.state);
      object.put("BODY", o);

      return status(errorCode.getStatus()).body(object);
    }
  }
}
