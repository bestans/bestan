package bestan.common.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;

import bestan.common.db.adapter.DatabaseAdapter;
import bestan.common.util.Utility;
import bestan.log.GLog;

/**
 * a database transaction
 */
public class DBTransaction {
    /** logger */
    private static Logger logger = GLog.log;

    /** 适配器 具有connection的一些基本调用  */
	private DatabaseAdapter databaseAdapter = null;
	
	/** 调用这次会话的线程即{@link bestan.common.db.command.DBCommandQueueBackgroundThread} */
	private Thread thread;

	/**
	 * Creates a new DBTransaction.
	 *
	 * @param databaseAdapter database adapter for accessing the database
	 */
	protected DBTransaction(DatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}

	/**
	 * sets the thread in which this transaction is supposed to be used.
	 *
	 * @param thread Thread
	 */
	protected void setThread(Thread thread)  {
		this.thread = thread;
	}

	/**
	 * prints an error if a DBTransaction is accessed outside the thread 
	 * it was bound to.
	 * 
	 * 检查这个Transaction线程是否与{@link bestan.common.db.command.DBCommandQueueBackgroundThread}相同
	 */
	private void ensureCorrectThread(boolean acceptUnused) {
		if (thread == null) {
			if (!acceptUnused) {
				logger.error("Accessing DBTransaction " + this + " that is supposed to be available", new Throwable());
			}
		} else if (thread != Thread.currentThread()) {
			logger.error("Transaction " + this + " is bound to thread " + thread.getName() + " is used in thread " + Thread.currentThread(), new Throwable());
			// 打印所有线程堆栈
			//Utility.dumpAllStacktrace(logger);
		}
	}

	/**
	 * tries to commits this transaction, in case the commit fails, a rollback is executed.
	 *
	 * @throws SQLException in case of an database error
	 */
	protected void commit() throws SQLException {
		ensureCorrectThread(false);
		databaseAdapter.commit();
	}

	/**
	 * rollback this transaction
	 *
	 * @throws SQLException in case of an database error
	 */
	protected void rollback() throws SQLException {
		ensureCorrectThread(false);
		databaseAdapter.rollback();
	}

	/**
	 * closes the database connection
	 */
	protected void close() {
		ensureCorrectThread(true);
		try {
			databaseAdapter.close();
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	/**
     * executes an SQL statement with parameter substitution
     *
     * @param query   SQL statement
     * @return number of affected rows
     * @throws SQLException in case of an database error 
     */
	public int execute(String query) throws SQLException {
		ensureCorrectThread(false);
		return databaseAdapter.execute(query);
	}
	
    /**
     * executes an SQL statement with parameter substitution
     *
     * @param query   SQL statement
     * @param inStream input streams to stream into "?" columns
     * @return number of affected rows
     * @throws SQLException in case of an database error 
     * @throws IOException in case of an input/output error
     */
	public int excuteUpdateByPrepare(String query, InputStream... inStream) throws SQLException, IOException {
		ensureCorrectThread(false);
		return databaseAdapter.executeUpdateByPrepared(query, inStream);
	}
	
	/**
     * executes an SQL statement with parameter substitution
     *
     * @param query   SQL statement
     * @return number of affected rows
     * @throws SQLException in case of an database error 
     * @throws IOException in case of an input/output error
     */
	public int excuteUpdate(String query) throws SQLException, IOException {
		ensureCorrectThread(false);
		return databaseAdapter.executeUpdate(query);
	}

    /**
     * queries the database
     *
     * @param query   SQL statement
     * @param params  parameter values
     * @return ResultSet
     * @throws SQLException in case of an database error 
     */
	public ResultSet query(String query) throws SQLException {
		ensureCorrectThread(false);
		return databaseAdapter.query(query);
	}

    /**
     * queries the database and returns the first column in the first row as integer (for example for a count(*)).
     *
     * @param query   SQL statement
     * @param params  parameter values
     * @return value of the first column in the first row
     * @throws SQLException in case of an database error 
     */
	public int querySingleCellInt(String query) throws SQLException {
		ensureCorrectThread(false);
		return databaseAdapter.querySingleCellInt(query);
	}

	/**
	 * gets the id of the last insert. Note: The table and idcolumn parameters
	 * <b>must</b> match the last insert statement. This is because on some
	 * database systems a SELECT IDENTITY is performed and on other database
	 * systems a SELECT curval(table_idcolumn_seq). 
	 *  
	 * @param table  name of table on which the last insert was done
	 * @param idcolumn name autoincrement serial column of that table
	 * @return generated id
	 * @throws SQLException in case of an database error
	 */
	public int getLastInsertId(String table, String idcolumn) throws SQLException {
		ensureCorrectThread(false);
		return databaseAdapter.getLastInsertId(table, idcolumn);
	}

	/**
	 * Prepares a statement for a batch operation.
	 *
     * @param query   SQL statement
     * @param params  parameter values
     * @return PreparedStatement
     * @throws SQLException in case of an database error 
	 */
	public PreparedStatement prepareStatement(String query) throws SQLException {
		ensureCorrectThread(false);
		return databaseAdapter.prepareStatement(query);
	}


	/**
	 * checks whether the specified table exists
	 *
	 * @param table name of table
	 * @return true, if the table exists, false otherwise
     * @throws SQLException in case of an database error
	 */
	public boolean doesTableExist(String table) throws SQLException {
		ensureCorrectThread(false);
		return databaseAdapter.doesTableExist(table);
	}

	/**
	 * checks whether the specified column exists
	 *
	 * @param table name of table
	 * @param column name of column
	 * @return true, if the column exists, false otherwise
     * @throws SQLException in case of an database error
	 */
	public boolean doesColumnExist(String table, String column) throws SQLException {
		ensureCorrectThread(false);
		return databaseAdapter.doesColumnExist(table, column);
	}
	
	/**
	 * Gets the length of the specified column
	 * @param table name of table
	 * @param column name of column
	 * @return the length of the column
	 * @throws SQLException in case of a database error
	 */
	public int getColumnLength(String table, String column) throws SQLException {
		ensureCorrectThread(false);
		return databaseAdapter.getColumnLength(table, column);
	}

}
