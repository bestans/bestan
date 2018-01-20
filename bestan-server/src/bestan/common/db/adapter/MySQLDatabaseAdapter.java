package bestan.common.db.adapter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import bestan.common.db.DatabaseConnectionException;
import bestan.common.log.LogManager;
import bestan.common.log.Logger;

/**
 * abstracts from MySQL specifications
 */
public class MySQLDatabaseAdapter extends AbstractDatabaseAdapter {
	private static Logger logger = LogManager.getLogger(MySQLDatabaseAdapter.class);

	// major version of the database
	private int majorVersion;

	/**
	 * creates a new MySQLDatabaseAdapter
	 *
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public MySQLDatabaseAdapter() throws DatabaseConnectionException {
		super();
	}

	/**
	 * 创建 Connection
	 */
	@Override
	protected Connection createConnection() throws SQLException, DatabaseConnectionException {
		try {
			Connection con = super.createConnection();
			DatabaseMetaData meta;
			meta = con.getMetaData();
			String name = meta.getDatabaseProductName();
			if (name.toLowerCase(Locale.ENGLISH).indexOf("mysql") < 0) {
				logger.warn("Using MySQLDatabaseAdapter to connect to " + name);
			}
			
			//======================================================================
			// set session info
			Statement statement = con.createStatement();
			try {
				// 设置innodb 等待锁的最长时间，配置表里是100毫秒
				statement.execute("SET SESSION innodb_lock_wait_timeout = 120;");
				// 设置session 等待时间  long long ...
				statement.execute("SET SESSION wait_timeout = 60*60*24*60*10;");
				// NO_AUTO_CREATE_USER: 禁止GRANT创建密码为空的用户
				// NO_ENGINE_SUBSTITUTION: 如果需要的存储引擎被禁用或未编译，那么抛出错误。不设置此值时，用默认的存储引擎替代，并抛出一个异常
				statement.execute("SET SESSION sql_mode = \"NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION\";");
				statement.execute("SET NAMES UTF8;");
			} finally {
				statement.close();
			}
			//======================================================================
			
			this.majorVersion = con.getMetaData().getDatabaseMajorVersion();
			return con;
		} catch (SQLException e) {

			// Shorten extremely long MySql error message, which contains the cause-Exception in getMessage() instead of getCause()
			// 连接超时或已达到idle时间 参见 {@link com.mysql.jdbc.SQLError}
			String msg = e.toString();
			if (msg.contains("CommunicationsException")) {
				int pos = msg.indexOf("BEGIN NESTED EXCEPTION");
				if (pos > -1) {
					throw new DatabaseConnectionException(msg.substring(0, pos - 3).trim());
				}
			}
			throw e;
		}
	}

	/**
	 * rewrites CREATE TABLE statements to add TYPE=InnoDB
	 *
	 * @param sql original SQL statement
	 * @return modified SQL statement
	 */
	@Override
	protected String rewriteSql(String sql) {
		String mySql = sql.trim();
		if (mySql.toLowerCase(Locale.ENGLISH).startsWith("create table")) {
			if (this.majorVersion >= 5) {
				mySql = sql.substring(0, sql.length() - 1) + " ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED;";
			} else {
				mySql = sql.substring(0, sql.length() - 1) + " TYPE=InnoDB;";
			}
		}
		return mySql;
	}

}
