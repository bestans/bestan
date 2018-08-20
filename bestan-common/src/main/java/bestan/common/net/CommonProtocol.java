package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.logic.ObjectManager;
import io.netty.channel.ChannelHandlerContext;

public class CommonProtocol extends AbstractProtocol {
	protected Message message;
	
	public CommonProtocol(ProtocolHeader header, Message message) {
		super(header);
		this.message = message;
	}
	
	public CommonProtocol(ChannelHandlerContext ctx, Message message) {
		super(new ProtocolHeader(0, ctx.channel().attr(NetConst.GUID_ATTR_INDEX).get(), ctx));
		this.message = message;
	}

	@Override
	public void run() {
		var obj = ObjectManager.getInstance().getObject(header.getGuid());
		if (obj != null) {
			obj.executeProtocol(header, message);
		}
	}

	@Override
	public byte[] encode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Message getMessage() {
		return message;
	}
}
