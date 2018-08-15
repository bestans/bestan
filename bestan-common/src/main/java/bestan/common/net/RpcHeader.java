package bestan.common.net;

import bestan.common.guid.Guid;
import io.netty.channel.ChannelHandlerContext;

public class RpcHeader extends ProtocolHeader {
	public long id = 0;
	public boolean isRequest = true;
	
	public RpcHeader(int msgId, Guid guid, ChannelHandlerContext ctx) {
		super(msgId, guid, ctx);
	}
}
