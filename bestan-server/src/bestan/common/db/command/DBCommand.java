package bestan.common.db.command;

import java.io.IOException;
import java.sql.SQLException;

import bestan.common.db.DBTransaction;

/**
 * a database command that can be processed asynchronously.
 */
public interface DBCommand {

	/**
	 * gets the exception in case one was thrown during processing.
	 *
	 * @return RuntimeException or <code>null</code> in case no exception was thrown.
	 */
	public Exception getException();

	/**
	 * processes the database request.
	 *
	 * @param transaction DBTransaction
	 * @throws SQLException in case of an database error
	 * @throws IOException in case of an input/output error
	 */
	public void execute(DBTransaction transaction) throws SQLException, IOException;

	/**
	 * remembers an exception
	 *
	 * @param exception RuntimeException
	 */
	public void setException(Exception exception);
}