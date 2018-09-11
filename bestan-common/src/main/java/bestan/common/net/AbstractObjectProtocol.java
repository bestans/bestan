package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.message.IMessageHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
public abstract class AbstractObjectProtocol extends AbstractProtocol {
	protected IMessageHandler handler;

	public AbstractObjectProtocol(ChannelHandlerContext ctx, int messageId, Message message) {
		super(ctx, messageId, message);
		// TODO Auto-generated constructor stub
	}

	@Override
	public long getThreadIndex() {
		return handler.getThreadIndex(message);
	}
	
}
