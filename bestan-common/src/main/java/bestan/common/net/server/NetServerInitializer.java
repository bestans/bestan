package bestan.common.net.server;

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
        pipeline.addLast(new NetDecodeHandler(serverManager.getConfig().baseMessage));
        pipeline.addLast(new NetEncodeHandler());

        // and then business logic.
        pipeline.addLast(new NetServerHandler(serverManager));
	}
}
