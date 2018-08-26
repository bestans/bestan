package bestan.common.db.util;

import bestan.common.db.RocksDBConfig;
import bestan.common.db.StorageEnv;
import bestan.common.logic.ServerConfig;
import bestan.common.module.IModule;

public class DBModule implements IModule{
	private final RocksDBConfig config;
	
	public DBModule(RocksDBConfig config) {
		this.config = config;
	}
	
	@Override
	public void startup(ServerConfig arg0) {
		StorageEnv.init(config.dbPath);
	}
	
	@Override
	public void close() {
		StorageEnv.close();
	}
}
