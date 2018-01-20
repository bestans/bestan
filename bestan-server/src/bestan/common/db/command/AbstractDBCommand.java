package bestan.common.db.command;

import java.io.IOException;
import java.sql.SQLException;

import bestan.common.db.DBTransaction;

/**
 * An abstract asynchronous database command.
 */
public abstract class AbstractDBCommand implements DBCommand {
	private Exception exception = null;

	/**
	 * gets the exception in case one was thrown during processing.
	 *
	 * @return RuntimeException or <code>null</code> in case no exception was thrown.
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * remembers an exception
	 *
	 * @param exception RuntimeException
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	/**
	 * processes the database request.
	 *
	 * @param transaction DBTransaction
	 * @throws SQLException in case of an database error
	 * @throws IOException in case of an input/output error
	 */
	public abstract void execute(DBTransaction transaction) throws SQLException, IOException;

}
