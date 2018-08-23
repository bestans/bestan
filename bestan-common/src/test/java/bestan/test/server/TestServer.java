package bestan.test.server;

import bestan.common.log.Glog;
import bestan.common.lua.LuaConfigs;
import bestan.common.message.MessageFactory;
import bestan.common.protobuf.MessageEnum;
import bestan.common.protobuf.Proto;

/**
 * @author yeyouhuan
 *
 */
public class TestServer {

	public static void main(String[] args) {
		Class<? extends Enum> cls = MessageEnum.class;
		Glog.debug("aa={},{},{}", Proto.BaseProto.BaseProto33.class.getEnclosingClass().isMemberClass(),
				Proto.BaseProto.class.getEnclosingClass().isMemberClass(),
				Proto.class.getEnclosingClass());
		if (2 != 1) {
			return;
		}
		var worker = new TestExecutor();

		LuaConfigs.loadConfig("E:/bestan/config/", "bestan.test.server");
		var cfg = LuaConfigs.get(TestNetServerConfig.class);
		cfg.workdExecutor = worker;
		cfg.baseProtocol = new TestProtocol();
		Glog.debug("TestNetServerConfig={}",cfg);
		MessageFactory.loadMessage("bestan.common.protobuf");
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
