package bestan.test.clientdownload;

import bestan.common.log.Glog;
import bestan.common.lua.LuaConfigs;
import bestan.common.message.MessageFactory.MessageModule;
import bestan.common.module.ModuleManager;
import bestan.common.net.client.BaseNetClientManager;
import bestan.common.thread.BThreadPoolExecutors;
import bestan.common.timer.BTimer.TimerModule;
import bestan.test.clientdownload.UpdateFileResHandler.UpdateFile;
import bestan.test.server.TestNetClientConfig;
import bestan.test.server.TestProtocol;

/**
 * @author yeyouhuan
 *
 */
public class TestDownloadClient {
	
	public static void main(String[] args) {
		String[] packages = {
				"bestan.test.server",
				"bestan.test.clientdownload",
		};
		if (!LuaConfigs.loadConfig("E:/bestan/config/", packages)) {
			return;
		}
		var cfg = LuaConfigs.get(TestNetClientConfig.class);
		cfg.workdExecutor = BThreadPoolExecutors.newMutipleSingleThreadPool(1);
		cfg.baseProtocol = new TestProtocol();
		Glog.debug("TestNetClientConfig={}",cfg);
		
		String[] handlerPackages = {
				"bestan.test.download",
				"bestan.test.clientdownload",
		};
		var messageModule = new MessageModule(null, null, handlerPackages);
		var timer = new TimerModule(cfg.workdExecutor, 100);
		var client = new BaseNetClientManager(cfg, cfg.workdExecutor, cfg.baseProtocol);
		ModuleManager.registerModule(true, messageModule, timer, client);
		ModuleManager.startup();
		
		try {
			UpdateFile.getInstance().request(client);
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ModuleManager.close();
	}
}
