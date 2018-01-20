package bestan.server;

import bestan.pb.NetCommon;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
	private final int READ_TIMEOUT = 5;
	
    public HttpServerInitializer() {
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new HttpProtobufDecoder(NetCommon.test_data.getDefaultInstance()));
        pipeline.addLast(new IdleStateHandler(READ_TIMEOUT, 0, 0));
        pipeline.addLast(new HttpServerhandler());
    }

}