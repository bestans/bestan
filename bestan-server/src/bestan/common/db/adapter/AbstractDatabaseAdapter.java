package bestan.common.db.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Properties;

import org.slf4j.Logger;

import bestan.common.config.ServerConfig;
import bestan.common.db.DatabaseConnectionException;
import bestan.log.GLog;

/**
 * abstract database adapter
 */
public abstract class AbstractDatabaseAdapter implements DatabaseAdapter {
	private static Logger logger = GLog.log;

	/** connection to the database */
	protected Connection connection;

	/** list of open statements */
	protected LinkedList<Statement> statements = null;
	/** list of open result sets */
	protected LinkedList<ResultSet> resultSets = null;

	/**
	 * creates a new AbstractDatabaseAdapter
	 *
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public AbstractDatabaseAdapter() throws DatabaseConnectionException {
		try {
			this.connection = createConnection();
		} catch (SQLException e) {
			throw new DatabaseConnectionException("Unable to create a connection to: " 
					+ ServerConfig.getInstance().db_driver, e);
		}

		this.statements = new LinkedList<Statement>();
		this.resultSets = new LinkedList<ResultSet>();
	}

	/**
	 * This method creates the real connection to database.
	 *
	 * @return a connection to the database
	 * @throws SQLException if the connection cannot be established.
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	protected Connection createConnection() throws SQLException, DatabaseConnectionException {
		// instantiate the Driver class
		try {
			if (ServerConfig.getInstance().db_driver != null) {
				Class.forName(ServerConfig.getInstance().db_driver).newInstance();
			}
		} catch (Exception e) {
			throw new DatabaseConnectionException("Cannot load driver class "
					+ ServerConfig.getInstance().db_driver, e);
		}

		// 配置连接属性
		Properties connectionInfo = new Properties();
		if (ServerConfig.getInstance().db_user != null) {
			connectionInfo.put("user", ServerConfig.getInstance().db_user);
		}
		if (ServerConfig.getInstance().db_passwd != null) {
			connectionInfo.put("password", ServerConfig.getInstance().db_passwd);
		}
		connectionInfo.put("charSet", "UTF-8");
		
		// 从jdbc驱动管理器中获取连接 
		Connection conn = DriverManager.getConnection(ServerConfig.getInstance().db_url, connectionInfo);

		// 设置 transaction 属性即连接属性  大部分属性已经通过 my.conf设置好了
		conn.setAutoCommit(false);
		// 设置 事务隔离级别，Repeatable-Read(默认) 就是一直读取上锁前的数据，不管对方锁是否释放。即读取快照（snap-shot）
		// 还有一个是Read-Commited 就是读取最新的数据，上锁的时候读取上锁前数据，如果对方那个锁释放了就读取最新的数据。 (以前是使用这种的隔离机智的，后来怕会有锁冲突改为默认的)
		// 还有一个是Read-Uncommited,就是未commit前也可以读取，这就是所谓的读取脏数据（错误的行为，因为还没有commit，与事务的隔离性矛盾）。 
		conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

		// 打印数据库基本信息
		DatabaseMetaData meta = conn.getMetaData();
		logger.debug("Connected to " + ServerConfig.getInstance().db_url + ": "
				+ meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion()
				+ " with driver " + meta.getDriverName() + " " + meta.getDriverVersion());

		return conn;
	}

	/**
	 * commit
	 */
	public void commit() throws SQLException {
		closeStatements();
		connection.commit();
	}

	/**
	 * rollback
	 */
	public void rollback() throws SQLException {
		closeStatements();
		connection.rollback();
	}

	/**
	 * 执行sql 并返回 影响的row num 使用Statement
	 * 这里使用了 excute 应该谨慎使用
	 */
	public int execute(String sql) throws SQLException {
		String mySql = rewriteSql(sql);
		int res = -2;
		Statement statement = connection.createStatement();
		try {
			boolean resultType = statement.execute(mySql);
			if (!resultType) {
				res = statement.getUpdateCount();
			}
		} finally {
			statement.close();
		}
		return res;
	}
	/**
	 * 执行sql 并返回 影响的row num 使用Statement
	 * 应该名成 update 包括 DDL INSERT UPDATE DELETE
	 */
	public int executeUpdate(String sql) throws SQLException, IOException {
		String mySql = rewriteSql(sql);
		int res = -2;
		Statement statement = connection.createStatement();
		try {
			res = statement.executeUpdate(mySql);
		} catch (SQLException e) {
			logger.error("error Sql:" + sql);
			throw e;
		} finally {
			statement.close();
		}
		return res;
	}
	
	/**
	 * 执行sql 并返回 影响的row num 使用PreparedStatement
	 * 应该名成 update 包括 DDL INSERT UPDATE DELETE
	 */
	public int executeUpdateByPrepared(String sql, InputStream... inputStreams) throws SQLException, IOException {
		String mySql = rewriteSql(sql);
		int res = -2;
		PreparedStatement statement = connection.prepareStatement(mySql);
		try {
			int i = 1; // yes, jdbc starts counting at 1.
			for (InputStream inputStream : inputStreams) {
				statement.setBinaryStream(i++, inputStream, inputStream.available());
			}
			res = statement.executeUpdate();
		} finally {
			statement.close();
		}
		return res;
	}

	/**
	 * 处理批量sql 并且么个sql上只有一个可变参数
	 * @param sql String eg: "select * from tb where a = ?; selcet * from tb where a = ?;"
	 */
	public void executeBatch(String sql, InputStream... inputStreams) throws SQLException, IOException {
		String mySql = rewriteSql(sql);
		PreparedStatement statement = connection.prepareStatement(mySql);
		try {
			int i = 1; // yes, jdbc starts counting at 1.
			for (InputStream inputStream : inputStreams) {
				statement.setBinaryStream(i, inputStream, inputStream.available());
				statement.executeUpdate();
			}
		} finally {
			statement.close();
		}
	}

	/**
	 * 执行sql 并返回结果集
	 */
	public ResultSet query(String sql) throws SQLException {
		String mySql = rewriteSql(sql);
		Statement stmt = connection.createStatement();
		try {
			ResultSet resultSet = stmt.executeQuery(mySql);
			addToGarbageLists(stmt, resultSet);
			return resultSet;
		} catch (RuntimeException e) {
			stmt.close();
			throw e;
		} catch (SQLException e) {
			stmt.close();
			throw e;
		}
	}

	/**
	 * 执行select 并返回第一个结果集的第一个 并且是int
	 */
	public int querySingleCellInt(String sql) throws SQLException {
		String mySql = rewriteSql(sql);
		int res = -1;
		Statement stmt = connection.createStatement();
		try {
			ResultSet resultSet = stmt.executeQuery(mySql);
			try {
				if(resultSet.next())
					res = resultSet.getInt(1);
			} finally {
				resultSet.close();
			}
		} finally {
			stmt.close();
		}
		return res;
	}

	/**
	 * Stores open statements and resultSets in garbages lists, so that
	 * they can be closed with one single close()-method
	 *
	 * 为了关闭时 统一删除掉没有关闭的 statement和result 应先关闭result 再关闭statement
	 *
	 * @param statement Statement
	 * @param resultSet ResultSet
	 */
	void addToGarbageLists(Statement statement, ResultSet resultSet) {
		statements.add(statement);
		resultSets.add(resultSet);
	}

	/**
	 * 关闭还没有清理的 resultset 和 statement
	 * @throws SQLException
	 */
	private void closeStatements() throws SQLException {
		// Note: Some JDBC drivers like Informix require resultSet.close()
		// before statement.close() although the second one is supposed to
		// close open ResultSets by itself according to the API doc.
		for (ResultSet resultSet : resultSets) {
			resultSet.close();
		}
		for (Statement statement : statements) {
			statement.close();
		}
		resultSets.clear();
		statements.clear();
	}

	/**
	 * 自动返回最后一个INSERT或 UPDATE 查询中 AUTO_INCREMENT列设置的第一个表发生的值
	 *  
	 * @warn 当一次insert多条记录时会返回其第一个insert的sequenceid
	 * @warn 独立于其他连接
	 */
	public int getLastInsertId(String table, String idcolumn) throws SQLException {
		String query = "select LAST_INSERT_ID() as inserted_id";
		return querySingleCellInt(query);
	}

	/**
	 * 关闭连接 即关闭了与mysql服务器的连接
	 */
	public void close() throws SQLException {
		closeStatements();
		connection.close();
	}

	/** 
	 * 使用此连接创建一个PreparedStatement
	 * 并把这个statement 放入到待删除statement容器中
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		String mySql = rewriteSql(sql);
		PreparedStatement statement = connection.prepareStatement(mySql);
		statements.add(statement);
		return statement;
	}

	/**
	 * 查看这个table是否存在
	 * DatabaseMetaData 是连接上的数据库信息
	 */
	public boolean doesTableExist(String table) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getTables(null, null, table, null);
		boolean res = result.next();
		result.close();
		return res;
	}

	/**
	 * 查看这个table是否存在column
	 */
	public boolean doesColumnExist(String table, String column) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns(null, null, table, column);
		boolean res = result.next();
		result.close();
		return res;
	}

	/**
	 * 获取column长度
	 */
	public int getColumnLength(String table, String column) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns(null, null, table, column);
		if (result.next()) {
			return result.getInt("COLUMN_SIZE");
		}
		return -1;
	}

	/**
	 * rewrites an SQL statement so that it is accepted by the database server software
	 * 由具体继承类来实现 这是一个虚函数
	 *
	 * @param sql original SQL statement
	 * @return modified SQL statement
	 */
	protected String rewriteSql(String sql) {
		return sql;
	}
}
