package bestan.common.db;

import java.lang.reflect.Constructor;

import bestan.common.config.ServerConfig;
import bestan.common.db.adapter.DatabaseAdapter;
import bestan.common.db.adapter.MySQLDatabaseAdapter;
import bestan.common.log.LogManager;
import bestan.common.log.Logger;

/**
 * creates DatabaseAdapters for the configured database system
 */
class AdapterFactory {
	private static Logger logger = LogManager.getLogger(AdapterFactory.class);

	/**
	 * creates a new AdapterFactory
	 *
	 * @param connInfo
	 */
	public AdapterFactory() {
	}

	/**
	 * creates a DatabaseAdapter
	 *
	 * @return DatabaseAdapter for the specified database
	 */
	@SuppressWarnings("unchecked")
    public DatabaseAdapter create() {
		try {
			String adapter = ServerConfig.getInstance().adapter;
			if (adapter == null) {
				return new MySQLDatabaseAdapter();
			}
			
			Class<DatabaseAdapter> clazz= (Class<DatabaseAdapter>) Class.forName(adapter);
			Constructor<DatabaseAdapter> ctor = clazz.getConstructor();
			return ctor.newInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}
