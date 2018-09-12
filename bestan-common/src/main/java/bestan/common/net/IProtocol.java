package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.event.IEvent;
import bestan.common.logic.IObject;
import io.netty.channel.ChannelHandlerContext;

public interface IProtocol extends IEvent{
	Message decode(byte[] data) throws Exception;
	Message encode(MessagePack message);
	IProtocol makeProtocol(ChannelHandlerContext ctx, Message message) throws Exception;
	MessagePack packMessage(IObject object, Message message);
	MessagePack packMessage(Message message);
}
