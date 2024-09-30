package art.heredium.hanabank;

public class HanaParamsDamagedException extends HanaParamsException {
	private static final long serialVersionUID = 8575047452485750122L;

	public HanaParamsDamagedException() {
		super();
	}

	public HanaParamsDamagedException(String message) {
		super(message);
	}

	public HanaParamsDamagedException(String message, Throwable cause) {
		super(message, cause);
	}

	public HanaParamsDamagedException(Throwable cause) {
		super(cause);
	}
}
