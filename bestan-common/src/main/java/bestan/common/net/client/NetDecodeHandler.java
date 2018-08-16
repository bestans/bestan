package bestan.common.net.client;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author yeyouhuan
 *
 */
public class NetDecodeHandler extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
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
