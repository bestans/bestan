package bestan.common.net.server;

import bestan.common.log.Glog;
import bestan.common.net.NetDecodeHandler;
import bestan.common.net.NetEncodeHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author yeyouhuan
 *
 */
public class NetServerInitializer extends ChannelInitializer<SocketChannel> {
	private BaseNetServerManager serverManager;
	
	public NetServerInitializer(BaseNetServerManager serverManager) {
		this.serverManager = serverManager;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Add the message codec first,
        pipeline.addLast("decode", new NetDecodeHandler(serverManager.getConfig().baseProtocol));
        pipeline.addLast("encode", new NetEncodeHandler(serverManager.getConfig().baseProtocol));

        // and then business logic.
        pipeline.addLast("serverHandler", new NetServerHandler(serverManager));
        
        Glog.debug("NetServerInitializer:pipeline={}", pipeline);
	}
}
