package bestan.common.net.client;

import bestan.common.net.NetDecodeHandler;
import bestan.common.net.NetEncodeHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author yeyouhuan
 *
 */
public class NetClientInitializer extends ChannelInitializer<SocketChannel> {
	private BaseNetClientManager client;
	
	public NetClientInitializer(BaseNetClientManager client) {
		this.client = client;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Add the message codec first,
        pipeline.addLast(new NetDecodeHandler(client.getConfig().baseMessage));
        pipeline.addLast(new NetEncodeHandler());

        // and then business logic.
        pipeline.addLast(new NetClientHandler(client));
	}
}
