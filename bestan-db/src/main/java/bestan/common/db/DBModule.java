package bestan.common.db;

import bestan.common.module.IModule;

public class DBModule implements IModule{
	private RocksDBConfig dbConfig;
	
	public DBModule(RocksDBConfig config) {
		this.dbConfig = config;
	}
	
	@Override
	public void startup() {
		StorageEnv.init(dbConfig);
	}
	
	@Override
	public void close() {
		StorageEnv.close();
	}
}
