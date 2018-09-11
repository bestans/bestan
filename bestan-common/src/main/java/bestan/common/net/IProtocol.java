package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.event.IEvent;
import io.netty.channel.ChannelHandlerContext;

public interface IProtocol extends IEvent{
	Message decode(byte[] data) throws Exception;
	Message encode(MessagePack message);
	IProtocol makeProtocol(ChannelHandlerContext ctx, Message message) throws Exception;
}
