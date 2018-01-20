package bestan.common.net.netty;

import java.util.concurrent.TimeUnit;

import bestan.common.config.ServerConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

	private NettyChannelProcessHandler test;
	public NettyServerInitializer() {
		test = new NettyChannelProcessHandler();
	}
	
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new NettyOutPutHandler());
        
        // @shareable
        p.addLast(NettyChannelHandleManager.getInstance());
        
        // 建议使用较长连接 借鉴腾讯WNS 系统架构
        if(0 < ServerConfig.getInstance().readTimeOut && 0 < ServerConfig.getInstance().writeTimeOut) {
        	p.addLast("idleStateHandler", new IdleStateHandler(ServerConfig.getInstance().readTimeOut, ServerConfig.getInstance().writeTimeOut, 0, TimeUnit.SECONDS));
            p.addLast("myIdleStateHandler", new IdleStateProcessHandler());
        }
        
        /** MessageDecodeHandler must not shareable */
        p.addLast(new NettyDecodeHandler()); 
        
     // @shareable
//      p.addLast(new NettyChannelProcessHandler()); 
        p.addLast(test);
    }
}

