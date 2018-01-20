package bestan.gameserver.net;

import bestan.common.net.message.AbstractMessageDispatcher;
import bestan.common.net.message.ProtoMessagePack;
import bestan.log.GLog;
import io.netty.channel.ChannelHandlerContext;

public class MessageDispatcher extends AbstractMessageDispatcher {
	private static MessageDispatcher instance;
	
	/**  
	 * Get Singleton Instance
	 */
	public static MessageDispatcher getDispatcher() {
		if (instance == null) {
			instance = new MessageDispatcher();
		}
		return instance;
	}
	
	@Override
	public void dispatchMessage(ChannelHandlerContext ctx, ProtoMessagePack msgPack) {
		MessageHandler handler = (MessageHandler) handlers.get(msgPack.getIntegerMessageId());
		if (null == handler) {
			GLog.log.error("invalid_message:msgid={}", msgPack.getIntegerMessageId());
			return;
		}
		handler.excute(ctx, msgPack.getMessage(), null);
	}
}
