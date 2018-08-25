package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.message.MessageFactory;
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
	
	@Override
	public long getThreadIndex() {
		return 0;
	}
	
	public Message getMessage() {
		return message;
	}
	
	public int getMessageId() {
		return messageId;
	}
	
	public ChannelHandlerContext getChannelHandlerContext() {
		return ctx;
	}
	
	@Override
	public void run() {
		try
		{
			var handle = MessageFactory.getMessageHandle(messageId);
			if (handle == null) {
				Glog.error("{} cannot find message handle:messageID={}", getClass().getSimpleName(), messageId);
				return;
			}
		
			handle.ProcessProtocol(this);
		} catch (Exception e) {
			Glog.error("{} ProcessProtocol Exception:messageID={}, Exception={}",
					getClass().getSimpleName(), messageId, e.getMessage());
		}
	}
}
