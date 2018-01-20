package bestan.common.net.netty.client;


import bestan.common.net.netty.NettyChannelProcessHandler;
import bestan.common.net.netty.NettyDecodeHandler;
import bestan.common.net.netty.NettyOutPutHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
	private NettyClient client = null;
	public NettyClientInitializer(NettyClient client) {
		this.client = client;
	}
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new NettyOutPutHandler());
        p.addLast(new NettyClientChannelHandleManager(client));
        p.addLast(new NettyDecodeHandler());
        p.addLast(new NettyChannelProcessHandler()); 
        
    
    }
}
