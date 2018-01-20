package bestan.gameserver.net;

import bestan.common.net.message.AbstractMessageDispatcher;
import bestan.common.net.message.IMessageHandlerRegister;
import bestan.gameserver.handler.CSTestDataHandler;

public class MessageHandlerRegister implements IMessageHandlerRegister {
	@Override
	public void handlerRegister(AbstractMessageDispatcher dispatcher) {
		dispatcher.register(1, new CSTestDataHandler());
	}
}
