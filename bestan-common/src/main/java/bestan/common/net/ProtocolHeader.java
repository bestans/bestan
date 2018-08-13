package bestan.common.net;

import bestan.common.guid.Guid;
import io.netty.channel.ChannelHandlerContext;

public class ProtocolHeader {
	protected Guid guid;
	protected ChannelHandlerContext ctx;
	
	public ProtocolHeader(Guid guid, ChannelHandlerContext channelHandlerContext) {
		this.guid = guid;
		this.ctx = channelHandlerContext;
	}
	
	public Guid getGuid() {
		return guid;
	}
	
	public ChannelHandlerContext getChannelHandlerContext() {
		return ctx;
	}
}
