package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.message.MessageFactory;
import bestan.common.net.handler.IMessageHandler;
import bestan.common.net.server.BaseNetServerManager;
import bestan.common.util.ExceptionUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractProtocol implements IProtocol {
	protected ChannelHandlerContext ctx;
	protected int messageId;
	protected Message message;
	
	public AbstractProtocol(ChannelHandlerContext ctx, int messageId, Message message) {
		this.ctx = ctx;
		this.messageId = messageId;
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}
	
	public int getMessageId() {
		return messageId;
	}
	
	public long getGuidValue() {
		return 0;
	}
	public ChannelHandlerContext getChannelHandlerContext() {
		return ctx;
	}
	public BaseNetServerManager getNetServer() {
		return null;
	}
	
	public ChannelFuture write(Message message) {
		return ctx.write(packMessage(message));
	}
	
	public ChannelFuture writeAndFlush(Message message) {
		return ctx.writeAndFlush(packMessage(message));
	}
	
	@Override
	public void run() {
		try
		{
			var handler = MessageFactory.getMessageHandle(messageId);
			if (handler == null) {
				Glog.error("{} cannot find message handle:messageID={},message={}", getClass().getSimpleName(), messageId, message);
				return;
			}
		
			runProtocol(handler);
		} catch (Exception e) {
			Glog.error("{} ProcessProtocol Exception:messageID={}, Exception={}, StackTrace={}",
					getClass().getSimpleName(), messageId, e.getMessage(), ExceptionUtil.getLog(e));
		}
	}
	
	protected void runProtocol(IMessageHandler handler) throws Exception {
		handler.processProtocol(this);
	}
}
