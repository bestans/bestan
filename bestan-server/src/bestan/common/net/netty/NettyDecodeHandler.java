package bestan.common.net.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

import bestan.common.net.message.ProtoMessagePack;

public class NettyDecodeHandler extends ByteToMessageDecoder {
	//private static final common.log.Logger logger = Log4J.getLogger(NettyDecodeHandler.class);

	/**
	 * Do decode
	 * 
	 * @param ctx ChannelHandlerContext this is a handler in netty pipeline a netty channel may be have some this handler
	 * @param in ByteBuf
	 * @param out List<Object>
	 */
	@Override
	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
		if(!in.isReadable() || in.readableBytes() <= 0) {
			return;
		}
		
		List<ProtoMessagePack> list = NettyDecode.get().decode(ctx.channel(), in);
		if(null != list) {
			for(ProtoMessagePack pack : list) {
				out.add(pack);
			}
			in.skipBytes(in.readableBytes());
		} else {
//			logger.debug("readable bytes:" + in.readableBytes());
			in.readBytes(in.readableBytes());
		}
	}
}
