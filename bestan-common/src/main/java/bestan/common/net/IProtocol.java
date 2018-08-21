package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.event.IEvent;
import io.netty.channel.ChannelHandlerContext;

public interface IProtocol extends IEvent{
	IProtocol decode(ChannelHandlerContext ctx, byte[] data) throws Exception;
	Message encode(Message message);
}
