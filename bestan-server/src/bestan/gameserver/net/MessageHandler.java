package bestan.gameserver.net;

import org.apache.http.HttpStatus;

import com.google.protobuf.Message;

import bestan.common.net.message.AbstractMessageHandler;
import bestan.gameserver.object.Player;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public abstract class MessageHandler extends AbstractMessageHandler {
	protected abstract void process(ChannelHandlerContext ctx, Message message, Player pPlayer);	
	protected boolean isBaned(ChannelHandlerContext ctx, Message message, Player pPlayer) {
		return false;
	}
	
	public void excute(ChannelHandlerContext ctx, Message message, Player pPlayer) {
		if(isBaned(ctx, message, pPlayer)) {
			return;
		}
		
		process(ctx, message, pPlayer);
	}
	
	public void writeResponse(ChannelHandlerContext ctx, Message message) {
		ByteBuf byteBuf = Unpooled.wrappedBuffer(message.toByteArray());
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
		ctx.writeAndFlush(response);
		ChannelFuture future = ctx.write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}
}
