package bestan.common.net.message;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractMessageDispatcher {
	/** message handler map. */
	protected static Map<Integer, AbstractMessageHandler> handlers = new HashMap<Integer, AbstractMessageHandler>();
	protected static Map<Integer, String> clazzMap = new HashMap<Integer, String>();
	
	public void register(int msgid, AbstractMessageHandler handler) {
		if(handlers.containsKey(msgid)) {
			return;
		}
		
		handlers.put(msgid, handler);
		clazzMap.put(msgid, handler.getClass().getName());
	}
	
	public void initMap(IMessageHandlerRegister dispatherRegister) {
		dispatherRegister.handlerRegister(this);
	}
	
	public abstract void dispatchMessage(ChannelHandlerContext ctx, ProtoMessagePack msgPack);
}
