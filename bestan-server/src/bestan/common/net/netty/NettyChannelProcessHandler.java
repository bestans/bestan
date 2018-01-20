package bestan.common.net.netty;

import bestan.common.net.message.ProtoMessagePack;
import bestan.common.server.LogicServerManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class NettyChannelProcessHandler extends SimpleChannelInboundHandler<ProtoMessagePack> {
//	private static final common.Logger logger = Log4J.getLogger(NettyChannelProcessHandler.class);
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
//		logger.debug("channelReadComplete");
//        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	if(cause instanceof ReadTimeoutException) {
//    		logger.debug("ReadTimeoutException ReadTimeoutException ReadTimeoutException");
    	}
        //cause.printStackTrace();
        ctx.close();
    }

	protected void channelRead0(ChannelHandlerContext arg0, ProtoMessagePack arg1) throws Exception {
		// this way is use the logic thread to do player logic
		arg1.setChannelContext(arg0);
		LogicServerManager.getInstance().getLogicServer().putToMessageQueue(arg1);
	}

	protected void messageReceived(ChannelHandlerContext ctx,
			ProtoMessagePack msg) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
