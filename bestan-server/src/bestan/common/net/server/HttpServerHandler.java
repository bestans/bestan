package bestan.common.net.server;

import bestan.common.net.message.ProtoMessagePack;
import bestan.common.server.LogicServerManager;
import bestan.log.GLog;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HttpServerHandler extends SimpleChannelInboundHandler<ProtoMessagePack> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProtoMessagePack msg) throws Exception {
		// TODO Auto-generated method stub
		GLog.log.debug("putToMessageQueue msgid={}", msg.getIntegerMessageId());
		msg.setChannelContext(ctx);
		LogicServerManager.getInstance().getLogicServer().putToMessageQueue(msg);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
		
		GLog.log.debug("aaaaa active");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
		
		GLog.log.debug("aaaaa inactive");
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }

        IdleStateEvent e = (IdleStateEvent) evt;
        if (e.state() == IdleState.READER_IDLE) {
            // The connection was OK but there was no traffic for last period.
            GLog.log.debug("Disconnecting due to no inbound traffic");
            ctx.close();
        }
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		GLog.log.debug("ctx exception:{}", cause.getMessage());
		ctx.close();
	}
}
