package bestan.gameserver.net;

import bestan.common.net.message.IMessageRegister;
import bestan.pb.NetCommon.test_data;

public class MessageRegister implements IMessageRegister {

	@Override
	public void messageRegister() {
		register(1, test_data.getDefaultInstance());
	}
}
