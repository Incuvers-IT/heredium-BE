package art.heredium.core.config.error.entity;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private Object body;
    private Integer state;

    public ApiException(ErrorCode errorCode, Object body) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
        this.body = body;
    }

    public ApiException(ErrorCode errorCode, Object body, Integer state) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
        this.body = body;
        this.state = state;
    }

    public ApiException(ErrorCode errorCode) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
    }
}
