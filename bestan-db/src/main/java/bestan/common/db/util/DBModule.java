package bestan.common.db.util;

import bestan.common.db.RocksDBOption;
import bestan.common.db.StorageEnv;
import bestan.common.module.IModule;

public class DBModule implements IModule{
	private final RocksDBOption config;
	
	public DBModule(RocksDBOption config) {
		this.config = config;
	}
	
	@Override
	public void startup() {
		StorageEnv.init(config.path);
	}
	
	@Override
	public void close() {
		StorageEnv.close();
	}
}
