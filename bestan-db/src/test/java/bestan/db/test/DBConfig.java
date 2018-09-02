package bestan.db.test;

import bestan.common.db.RocksDBConfig;
import bestan.common.log.Glog;
import bestan.common.lua.BaseLuaConfig;

/**
 * @author yeyouhuan
 *
 */
public class DBConfig extends RocksDBConfig{

	public static void setInstance(BaseLuaConfig config) {
		Glog.debug("aaaa");
	}
}
