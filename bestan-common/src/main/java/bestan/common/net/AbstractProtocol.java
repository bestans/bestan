package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.message.MessageFactory;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractProtocol implements IProtocol {
	protected int messageID;
	protected Message message;
	protected ChannelHandlerContext ctx;
	
	protected abstract Message getBaseMessage();
	protected abstract AbstractProtocol newProtocol(ChannelHandlerContext ctx, Message message);
	
	public AbstractProtocol(ChannelHandlerContext ctx, Message message) {
		this.ctx = ctx;
		this.message = message;
		this.messageID =  MessageFactory.getMessageIndex(message);
	}
	
	@Override
	public long getThreadIndex() {
		return 0;
	}
	
	private Message decodeMessage(byte[] data) throws Exception {
		return getBaseMessage().newBuilderForType().mergeFrom(data).build();
	}

	@Override
	public IProtocol decode(ChannelHandlerContext ctx, byte[] data) throws Exception {
		var msg = decodeMessage(data);
		return newProtocol(ctx, msg);
	}

	public AbstractProtocol getDefaultInstance() {
		return newProtocol(null, null);
	}
	
	public Message getMessage() {
		return message;
	}
	
	public int getMessageID() {
		return messageID;
	}
	
	public ChannelHandlerContext getChannelHandlerContext() {
		return ctx;
	}
}
