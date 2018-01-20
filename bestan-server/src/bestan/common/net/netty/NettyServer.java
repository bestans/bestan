package bestan.common.net.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import bestan.common.config.ServerConfig;
import bestan.common.server.MainServer.E_SERVER_TYPE;


public class NettyServer {	
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
	
	private String serverIP;
	private int serverPort;
	private int netThreadCount;
		
	
	/**
	 * 当前 Connection size 包括 iweb 以及 其他
	 * @return
	 */
	public int currentOnlineNums() {
		return ALL_CHANNELS.size();
	}
	
	/** 
	 * Constructor
	 * 
	 * just do CleanUp
	 */
	public NettyServer(E_SERVER_TYPE eServerType) {
		cleanUp();
		this.netThreadCount = ServerConfig.getInstance().logicThreadNum;
		
		switch (eServerType) {
		case GameServer:
			this.serverIP = ServerConfig.getInstance().gsIP;
			this.serverPort = ServerConfig.getInstance().gsPort;
			break;
		case WorldServer:
			this.serverIP = ServerConfig.getInstance().worldServerIP;
			this.serverPort = ServerConfig.getInstance().worldServerPort;
			break;
		case BattleServer:
			this.serverIP = ServerConfig.getInstance().battleServerIP;
			this.serverPort = ServerConfig.getInstance().battleServerPort;
			break;
		case HttpAgent:
			this.serverIP = ServerConfig.getInstance().httpAgentIP;
			this.serverPort = ServerConfig.getInstance().httpAgentPort;
		default:
			break;
		}
		
		
	}
	
	/** 
	 * CleanUp
	 */
	private void cleanUp() {
		this.serverBootstrap = null;
		this.socketAddress = null;
		this.bossGroup = null;
		this.workerGroup = null;
		this.serverChannel = null;
	}
	
	/** 
	 * stopServer
	 */
	public void stopServer() throws Exception {
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
		return "NettyServer [socketAddress=" + socketAddress.getAddress().getAddress().toString() + ", port=" + socketAddress.getPort() + "]";
	}

	/** 
	 * init netty server 
	 * 
	 * @param InetSocketAddress
	 * @param int workerNum  worker thread nums
	 */
	public void initServer(InetSocketAddress address, int workerNum) {
		this.bossGroup = new NioEventLoopGroup(1);  // (1)
        this.workerGroup = new NioEventLoopGroup(workerNum); // (2)
        this.socketAddress = address;
        
        this.serverBootstrap = new ServerBootstrap();   // (3)
        serverBootstrap.group(bossGroup, workerGroup)   // (4)
         			   .channel(NioServerSocketChannel.class)  // (5)
                       .childHandler(new NettyServerInitializer()) // (6)
                       .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)  // (7)
                       //.option(ChannelOption.SO_BACKLOG, 128)
                       .option(ChannelOption.SO_REUSEADDR, true)
                       .option(ChannelOption.SO_KEEPALIVE, false)
                       .option(ChannelOption.SO_RCVBUF, 65535)
                       .option(ChannelOption.SO_SNDBUF, 65535)
                       .childOption(ChannelOption.SO_KEEPALIVE, false)  // (8)
                       .childOption(ChannelOption.TCP_NODELAY, true)
                       .childOption(ChannelOption.SO_RCVBUF, 10240)
                       .childOption(ChannelOption.SO_SNDBUF, 40960)
                       .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                       // UnpooledByteBufAllocator
                       
                     //  .childOption(ChannelOption.SO_TIMEOUT, 200)
        			   .childOption(ChannelOption.SO_LINGER, 0); 
	}
	
	/** 
	 * start netty server 
	 * 
	 * @throws Exception (include NullPointerException)
	 */
	public void startServer() throws Exception {
		if(null == this.serverBootstrap) {
			throw new NullPointerException();
		}

		try {
			this.serverChannel = this.serverBootstrap.bind(this.socketAddress).sync().channel();
		} catch (InterruptedException e) {
			throw e;
		}
		
	}
	

	public void start() throws Exception {
		initServer(new InetSocketAddress(this.serverIP, this.serverPort), this.netThreadCount);
		startServer();
	}
	
	
}
