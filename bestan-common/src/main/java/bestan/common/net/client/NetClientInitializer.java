package bestan.common.net.client;

import io.netty.channel.ChannelInitializer;
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
		
	}
}
