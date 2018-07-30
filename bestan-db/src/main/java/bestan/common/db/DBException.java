package bestan.common.db;

public class DBException extends RuntimeException {

	public enum ErrorCode{
		DB_UNKNOW,
		DB_RELOCK,
	}
	private static final long serialVersionUID = 5349958643181347186L;
	
	private ErrorCode errorCode = ErrorCode.DB_UNKNOW;
	
	public DBException (ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	public String getErrorCodeMessage() {
		return errorCode.name();
	}
}
