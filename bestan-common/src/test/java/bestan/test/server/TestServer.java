package bestan.test.server;

import bestan.common.log.Glog;
import bestan.common.lua.LuaConfigs;
import bestan.common.thread.BThreadPoolExecutors;

/**
 * @author yeyouhuan
 *
 */
public class TestServer {

	public static void main(String[] args) {
		var worker = BThreadPoolExecutors.newDirectExecutor();

		LuaConfigs.loadConfig("E:/bestan/config/", "bestan.test.server");
		var cfg = LuaConfigs.get(TestNetServerConfig.class);
		Glog.debug("TestNetServerConfig={}",cfg);
		try {
			new NetServer(cfg).start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
