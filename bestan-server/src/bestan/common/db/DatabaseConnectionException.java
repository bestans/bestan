package bestan.common.db;

/**
 * This exception is thrown when the database connection cannot be established.
 */
public class DatabaseConnectionException extends IllegalStateException {

	private static final long serialVersionUID = -4364194172882791418L;

	/**
	 * Creates a new DatabaseConnectionFailedException
	 *
	 * @param msg error message
	 */
	public DatabaseConnectionException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new DatabaseConnectionFailedException
	 *
	 * @param msg error message
	 * @param cause exception that generated this one.
	 */
	public DatabaseConnectionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Creates a new DatabaseConnectionFailedException
	 *
	 * @param cause exception that generated this one.
	 */
	public DatabaseConnectionException(Throwable cause) {
		super("Error connecting to datasebase.", cause);
	}

	/**
 	 * Creates a new DatabaseConnectionFailedException
	 */
	public DatabaseConnectionException() {
		super("Error connecting to datasebase.");
	}
}
