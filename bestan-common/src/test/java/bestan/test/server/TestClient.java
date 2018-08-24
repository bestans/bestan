package bestan.test.server;

import java.nio.charset.Charset;

import com.google.common.hash.Hashing;
import com.google.protobuf.ByteString;

import bestan.common.log.Glog;
import bestan.common.lua.LuaConfigs;
import bestan.common.message.MessageFactory;
import bestan.common.protobuf.MessageEnum;
import bestan.common.protobuf.Proto;

public class TestClient {

	public static void main(String[] args) {
		var worker = new TestExecutor();


		Glog.debug("xx={}", Hashing.sha256().hashString("absdfasdfagadse", Charset.defaultCharset()).hashCode());;
		LuaConfigs.loadConfig("E:/bestan/config/", "bestan.test.server");
		var cfg = LuaConfigs.get(TestNetClientConfig.class);
		cfg.workdExecutor = worker;
		cfg.baseProtocol = new TestProtocol();
		Glog.debug("TestNetServerConfig={}",cfg);
		MessageFactory.loadMessageIndex(MessageEnum.class);
		MessageFactory.loadMessage("bestan.common.protobuf");
		MessageFactory.loadMessageHandle("bestan.test.server");
		//MessageFactory.loadMessageHandle(packageName)
		var client = new NetClient(cfg);
		client.start();
		
		var msg = Proto.BaseProto.newBuilder();
		msg.setMessageId(1234);
		msg.setMessageData(ByteString.copyFromUtf8("aaaaa"));
		while (true) {
			try {
				Thread.sleep(1000);
				client.sendMessage(msg.build());
				Thread.sleep(1000);
				break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			client.stop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
