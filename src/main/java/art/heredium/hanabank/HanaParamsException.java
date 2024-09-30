package art.heredium.hanabank;

public class HanaParamsException extends Exception {
	private static final long serialVersionUID = -8940627410545532634L;

	public HanaParamsException() {
		super();
	}

	public HanaParamsException(String message) {
		super(message);
	}

	public HanaParamsException(String message, Throwable cause) {
		super(message, cause);
	}

	public HanaParamsException(Throwable cause) {
		super(cause);
	}
}
