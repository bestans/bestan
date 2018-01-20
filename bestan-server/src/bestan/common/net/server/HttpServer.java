package bestan.common.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import bestan.common.datastruct.ServerOption;

public class HttpServer extends AbstractServer {	
	/** the Channel Group */
	public final static ChannelGroup ALL_CHANNELS = new DefaultChannelGroup("SERVER-CHANNELS", null);
	
	/** Server SocketAddress */
	protected InetSocketAddress socketAddress;
	
	/** Netty Server start Main Class */
	protected ServerBootstrap serverBootstrap;
	
	/** Netty Server bossgroup apply 1 thread */
	protected EventLoopGroup bossGroup;
	
	/** Netty Server bossgroup apply thread num will be set in server.ini */
	protected EventLoopGroup workerGroup;
	
	/** Netty Channel is equals nio socketchannle or connection */
	protected Channel serverChannel;
		
	/**
	 * 当前 Connection size 包括 iweb 以及 其他
	 * @return
	 */
	public int currentOnlineNums() {
		return ALL_CHANNELS.size();
	}
	
	public HttpServer(ServerOption option) {
		super(option);

		bossGroup = new NioEventLoopGroup(1);  // (1)
        workerGroup = new NioEventLoopGroup(option.netThreadCount); // (2)
        
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)   // (4)
					.channel(NioServerSocketChannel.class)  // (5)
					.childHandler(new HttpServerInitializer()) // (6)
					.option(ChannelOption.SO_BACKLOG, 1024)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true);
	}
	
	/** 
	 * stopServer
	 */
	public void stop() throws Exception {
		// close server Channel
		serverChannel.close().sync();
		
		//free boss thread and worker thread
		bossGroup.shutdownGracefully().sync();
		workerGroup.shutdownGracefully().sync();
		
		serverBootstrap = null;
	}

	/** 
	 * get ServerBootstrap
	 * 
	 * @return ServerBootstrap
	 */
	public ServerBootstrap getServerBootstrap() {
		return serverBootstrap;
	}

	/** 
	 * set ServerBootstrap
	 * 
	 * @param serverBootstrap
	 */
	public void setServerBootstrap(ServerBootstrap serverBootstrap) {
		this.serverBootstrap = serverBootstrap;
	}

	/** 
	 * get server socket address
	 * 
	 * @return InetSocketAddress
	 */
	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	/** 
	 * set server socket address
	 * 
	 * @param InetSocketAddress
	 */
	public void setInetAddress(InetSocketAddress inetAddress) {
		this.socketAddress = inetAddress;
	}

	/** 
	 * override toString
	 */
	@Override
	public String toString() {
		return "HttpServer [socketAddress=" + option.serverIP + ", port=" + option.serverPort + "]";
	}
	
	/** 
	 * start netty server 
	 * 
	 * @throws Exception (include NullPointerException)
	 */
	public void start() throws Exception {
		if(null == serverBootstrap) {
			throw new NullPointerException();
		}

		serverChannel = serverBootstrap.bind(new InetSocketAddress(option.serverIP, option.serverPort)).sync().channel();
	}
}
