package bestan.common.config;

import java.io.IOException;

import org.slf4j.Logger;

import bestan.common.log.Glog;
import bestan.common.reload.IHotdeploy;
import bestan.common.reload.ReloadResult;
import bestan.common.util.Global;

public class RedisConfig implements IHotdeploy {
	private static Logger logger = Glog.log;
	
	private static RedisConfig instance;
	
	protected RedisConfig() {
	}
	
	public static RedisConfig getInstance() {
		if(null == instance) {
			instance = new RedisConfig();
		}
		
		return instance;
	}
	
	public int pool_max_active;
	public int pool_max_idle;
	public int pool_max_wait; 
	public boolean pool_test_on_borrow;
	public boolean pool_test_on_return;

	public String ip;
	public int port;
	public String passwd;
	
	public int bgThreadNum;
	public int database;
	public int timeOut;	
	
	/**
	 * Load RedisConfig
	 * @throws IOException
	 */
	public boolean load() {
		try {
			ConfigurationParams param = new ConfigurationParams(false, Global.CONFIG_PATH, Global.REDIS_CONFIG);
			Configuration reader = new Configuration(param);
		
			this.ip   = reader.getString("IP");
			this.port = reader.getInt("Port");
			this.passwd = reader.getString("Passwd");
			this.pool_max_active = reader.getInt("MaxActive");
			this.pool_max_idle = reader.getInt("MaxIdle");
			this.pool_max_wait = reader.getInt("MaxWait");
			this.pool_test_on_borrow = reader.getInt("TestOnBorrow") == 1;
			this.pool_test_on_return = reader.getInt("TestOnReturn") == 1;
			this.database = reader.getInt("DataBase");
			this.bgThreadNum = reader.getInt("BGThreadNum");
			this.timeOut = reader.getInt("TimeOut");
			
			reader.clear();
		} catch(IOException e) {
			logger.error("Config load failed...");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public ReloadResult reload() throws Exception {
		ReloadResult ret = new ReloadResult(true);
		ret.setSuccess(load());
		if(!ret.isSuccess()) {
			ret.appendMsg("reload ini failed");
		} else {
			ret.appendMsg("reload ini ok");
		}
		
		return ret;
	}
}
