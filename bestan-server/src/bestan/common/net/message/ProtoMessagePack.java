package bestan.common.net.message;

import io.netty.channel.ChannelHandlerContext;
import com.google.protobuf.Message;
import bestan.common.protobuf.NetBase.BaseMessage;

public class ProtoMessagePack implements IMessagePack {
	private BaseMessage header;
	private Message msg;
	private ChannelHandlerContext ctx;
	
	public ProtoMessagePack(BaseMessage header, Message msg) {
		this.header = header;
		this.msg = msg;
		this.ctx = null;
	}

	public Message getMessage() {
		return this.msg;
	}
	
	public int getMessageId() {
		return header.getType();
	}
	
	public Integer getIntegerMessageId() {
		return header.getType();
	}	
	
	public ChannelHandlerContext getChannelContext() {
		return this.ctx;
	}
		
	public void setChannelContext(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	//========================================
}
