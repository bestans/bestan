package bestan.test.download;

import bestan.common.download.FileManager.FileManagerModule;
import bestan.common.log.Glog;
import bestan.common.logic.Gmatrix.GmatrixModule;
import bestan.common.lua.LuaConfigs;
import bestan.common.message.MessageFactory.MessageModule;
import bestan.common.module.ModuleManager;
import bestan.common.net.server.BaseNetServerManager;
import bestan.common.thread.BThreadPoolExecutors;
import bestan.common.timer.BTimer.TimerModule;
import bestan.test.server.TestNetServerConfig;
import bestan.test.server.TestProtocol;

/**
 * @author yeyouhuan
 *
 */
public class TestServer {

	public static void main(String[] args) {
		String[] packages = {
				"bestan.test.server",
				"bestan.test.download",
		};
		if (!LuaConfigs.loadConfig("E:/bestan/config/", packages)) {
			return;
		}
		var serverConfig = LuaConfigs.get(TestServerConfig.class);
		var cfg = LuaConfigs.get(TestNetServerConfig.class);
		cfg.workdExecutor = BThreadPoolExecutors.newMutipleSingleThreadPool(5);
		cfg.baseProtocol = new TestProtocol();
		Glog.debug("TestNetServerConfig={}",cfg);
		var fcfg = LuaConfigs.get(TestFileResourceConfig.class);
		Glog.debug("FileResourceConfig={}",fcfg);
		
		String[] handlerPackages = {
				"bestan.test.download",
		};
		var gmatrixModule = new GmatrixModule(serverConfig.serverConfig);
		var messageModule = new MessageModule(null, null, handlerPackages);
		var timer = new TimerModule(cfg.workdExecutor, 100);
		var server = new BaseNetServerManager(cfg, cfg.workdExecutor, cfg.baseProtocol);
		var fileMan = new FileManagerModule(fcfg, server);
		ModuleManager.registerModule(true, gmatrixModule, messageModule, timer, server, fileMan);
		ModuleManager.startup();
	}
}
