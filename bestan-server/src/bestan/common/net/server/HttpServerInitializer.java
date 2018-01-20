package bestan.common.net.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
	static final int READ_TIMEOUT = 20;
	
    public HttpServerInitializer() {
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new HttpProtobufDecoder());
        pipeline.addLast(new IdleStateHandler(READ_TIMEOUT, 0, 0));
        pipeline.addLast(new HttpServerHandler());
    }

}