package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.message.MessageFactory;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractProtocol implements IProtocol {
	protected ProtocolHeader header;
	protected int messageID;
	protected Message.Builder message;
	protected ChannelHandlerContext ctx;
	
	public AbstractProtocol(ProtocolHeader header, ChannelHandlerContext ctx, Message.Builder message) {
		this.ctx = ctx;
		this.message = message;
	}
	
	@Override
	public long getThreadIndex() {
		return 0;
	}
	
	public Message.Builder getMessage() {
		return message;
	}
	
	public int getMessageID() {
		return messageID;
	}
	
	public ChannelHandlerContext getChannelHandlerContext() {
		return ctx;
	}
	
	@Override
	public void run() {
		if (header.isRpc) {
			//runRpc();
		} else {
			runProtocol();
		}
	}
	
	public void runProtocol() {
		try
		{
			var handle = MessageFactory.getMessageHandle(messageID);
			if (handle == null) {
				Glog.error("{} cannot find message handle:messageID={}", getClass().getSimpleName(), messageID);
				return;
			}
		
			handle.ProcessProtocol(this);
		} catch (Exception e) {
			Glog.error("{} ProcessProtocol Exception:messageID={}, Exception={}",
					getClass().getSimpleName(), messageID, e.getMessage());
		}
	}
}
