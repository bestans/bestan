package bestan.common.net;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author yeyouhuan
 *
 */
public class NetEncodeHandler extends MessageToByteEncoder<Message> {
	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
		var data = msg.toByteArray();
		//var data = ((IProtocol)msg).encode();
		out.writeInt(data.length);
		out.writeBytes(data);
		Glog.debug("NetEncodeHandler:size={}", data.length);
	}
}
