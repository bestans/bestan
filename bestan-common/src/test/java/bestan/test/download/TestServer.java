package bestan.test.download;

import bestan.common.log.Glog;
import bestan.common.lua.LuaConfigs;
import bestan.common.message.MessageFactory;
import bestan.common.protobuf.MessageEnum;
import bestan.test.server.TestExecutor;
import bestan.test.server.TestNetServerConfig;
import bestan.test.server.TestProtocol;

/**
 * @author yeyouhuan
 *
 */
public class TestServer {

	public static void main(String[] args) {
		var worker = new TestExecutor();

		LuaConfigs.loadConfig("E:/bestan/config/", "bestan.test.server");
		var cfg = LuaConfigs.get(TestNetServerConfig.class);
		cfg.workdExecutor = worker;
		cfg.baseProtocol = new TestProtocol();
		Glog.debug("TestNetServerConfig={}",cfg);
		MessageFactory.loadMessageIndex(MessageEnum.class);
		MessageFactory.loadMessage("bestan.common.protobuf");
		MessageFactory.loadMessageHandle("bestan.test.server");
	}
}
