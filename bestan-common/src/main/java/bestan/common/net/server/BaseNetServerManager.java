package bestan.common.net.server;

import java.net.InetSocketAddress;

import bestan.common.net.INetManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * option 和childoption 的区别：<p>
 * The parameters that we set using ServerBootStrap.option apply to <p>
 * the ChannelConfig of a newly created ServerChannel,<p>
 * i.e., the server socket which listens for and accepts the client connections.<p>
 * These options will be set on the Server Channel when bind() or connect() method is called.<p>
 * This channel is one per server.And the ServerBootStrap.childOption applies to to a channel's<p>
 * channelConfig which gets created once the serverChannel accepts a client connection. <p>
 * This channel is per client (or per client socket).So ServerBootStrap.option parameters <p>
 * apply to the server socket (Server channel) that is listening for connections and <p>
 * ServerBootStrap.childOption parameters apply to the socket that gets created once the connection <p>
 * is accepted by the server socket.The same can be extended to attr vs childAttr and <p>
 * handler vs childHandler methods in the ServerBootstrap class.How could I know which option <p>
 * should be an option and which should be a childOption ?Which ChannelOptions are supported <p>
 * depends on the channel type we are using. 
 * @author yeyouhuan
 *
 */
public abstract class BaseNetServerManager implements INetManager {
	private NetServerConfig config;
	protected ServerBootstrap serverBootstrap;
	protected EventLoopGroup bossGroup;
	protected EventLoopGroup workerGroup;
	protected Channel serverChannel;
	
	public abstract void onAddChannel(ChannelHandlerContext ctx);
	public abstract void onDelChannel(ChannelHandlerContext ctx);

	public BaseNetServerManager(NetServerConfig config) {
		this.config = config;
		bossGroup = new NioEventLoopGroup(config.bossGroupThreadCount);
        workerGroup = new NioEventLoopGroup();
        
        serverBootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
         			   .channel(NioServerSocketChannel.class)
                       .childHandler(new NetServerInitializer(this))
                       .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                       .option(ChannelOption.SO_RCVBUF, config.optionRcvbuf)
                       .option(ChannelOption.SO_SNDBUF, config.optionSndbuf)
                       .childOption(ChannelOption.TCP_NODELAY, true) //从而最小化报文传输延时
                       .childOption(ChannelOption.SO_RCVBUF, config.childOptionRcvbuf)
                       .childOption(ChannelOption.SO_SNDBUF, config.childOptionSndbuf)
                       .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
	}
	public void start() throws InterruptedException {
		if(null == serverBootstrap) {
			throw new NullPointerException();
		}

		var address = new InetSocketAddress(config.serverIP, config.serverPort);
		serverChannel = this.serverBootstrap.bind(address).sync().channel();
	}
	
	public void stop() throws InterruptedException {
		// close server Channel
		serverChannel.close().sync();
		
		//free boss thread and worker thread
		bossGroup.shutdownGracefully().sync();
		workerGroup.shutdownGracefully().sync();
		
		serverBootstrap = null;
	}
	
	public NetServerConfig getConfig() {
		return config;
	}
}
