package bestan.common.db;

import java.io.IOException;
import java.sql.SQLException;

import bestan.common.log.LogManager;

/**
 * 数据库模块启动类
 */
public class DatabaseBoostrap {

	/** the logger instance. */
	private static final bestan.common.log.Logger logger = LogManager.getLogger(DatabaseBoostrap.class);
	
	public DatabaseBoostrap() {
	}
	
	/**
	 * initializes the database system
	 *
	 * @throws DatabaseConnectionException in case the database configuration is broken
	 */
	public void initializeDatabase(IDatabaseInitialSchema databaseInitialSchema) throws DatabaseConnectionException {
		try {
			internalInit(databaseInitialSchema);
		} catch (Exception e) {
			logger.error("Failed to load configuration for database", e);
			throw new DatabaseConnectionException(e);
		}
	}

	/**
	 * 初始化
	 */
	private void internalInit(IDatabaseInitialSchema databaseInitialSchema) {
		try {
			if (TransactionPool.get() == null) {
				// 连接池初始化
				TransactionPool pool = new TransactionPool();
				// 注册连接池为全局
				pool.registerGlobally();
				// 更新数据库升级脚本
				initializeDatabaseSchema(databaseInitialSchema);
			}
		} catch (Exception e) {
			logger.error("Failed to initialize database", e);
			throw new DatabaseConnectionException(e);
		}
	}

	/**
	 * 执行数据库升级脚本
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void initializeDatabaseSchema(IDatabaseInitialSchema databaseInitialSchema) throws SQLException, IOException {
		final DBTransaction transaction = TransactionPool.get().getFreeDbTransaction();
		try {
			if(null != databaseInitialSchema) {
				databaseInitialSchema.onServerStartInitialSchema(transaction);
			}
			
			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			TransactionPool.get().rollback(transaction);
			throw e;
		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
			TransactionPool.get().rollback(transaction);
			throw ioe;
		}
	}
}
