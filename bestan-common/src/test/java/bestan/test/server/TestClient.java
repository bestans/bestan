package bestan.test.server;

import com.google.protobuf.ByteString;

import bestan.common.log.Glog;
import bestan.common.lua.LuaConfigs;
import bestan.common.protobuf.Proto;

public class TestClient {

	public static void main(String[] args) {
		var worker = new TestExecutor();

		LuaConfigs.loadConfig("E:/bestan/config/", "bestan.test.server");
		var cfg = LuaConfigs.get(TestNetClientConfig.class);
		cfg.workdExecutor = worker;
		cfg.baseMessage = Proto.BaseProto.getDefaultInstance();
		Glog.debug("TestNetServerConfig={}",cfg);
		var client = new NetClient(cfg);
		client.start();
		
		var msg = Proto.BaseProto.newBuilder();
		msg.getHeaderBuilder().setMessageId(1234);
		msg.setMessageData(ByteString.copyFromUtf8("aaaaa"));
		
		while (true) {
			try {
				Thread.sleep(10000);
				client.sendMessage(msg.build());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
