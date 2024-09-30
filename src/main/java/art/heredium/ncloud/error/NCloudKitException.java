package art.heredium.ncloud.error;

public class NCloudKitException extends RuntimeException {
    private final NCloudKitErrorCode errorCode;
    private Object body;

    public NCloudKitException(NCloudKitErrorCode errorCode) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
        this.body = "";
    }

    public NCloudKitException(NCloudKitErrorCode errorCode, Object body) {
        super(errorCode.name(), null, false, false);
        this.errorCode = errorCode;
        this.body = body;
    }
}
