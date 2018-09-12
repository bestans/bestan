package bestan.common.net.client;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.logic.IObject;
import bestan.common.module.IModule;
import bestan.common.net.BaseNetManager;
import bestan.common.net.IProtocol;
import bestan.common.net.RpcManager.RpcObject;
import bestan.common.thread.BExecutor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author yeyouhuan
 *
 */
public class BaseNetClientManager extends BaseNetManager implements IModule {
	private AtomicBoolean connecting = new AtomicBoolean(false);
	private Channel channel;
	private ChannelHandlerContext ctx;
	
	protected NetClientConfig config;
	protected EventLoopGroup bossGroup;
	protected EventLoopGroup workerGroup;
	protected Bootstrap bootStrap;

	/**
	 * @param config client配置
	 * @param executor 消息处理的工作线程池
	 * @param protocol 解析/编码消息的方式
	 */
	public BaseNetClientManager(NetClientConfig config, BExecutor executor, IProtocol protocol) {
		super(protocol);
		this.config = config;
		this.config.baseProtocol = protocol;
		this.config.workdExecutor = executor;
		workerGroup = new NioEventLoopGroup(1);
		bootStrap = new Bootstrap().group(workerGroup)
		    	      .channel(NioSocketChannel.class)
			    	  .handler(new NetClientInitializer(this))
			    	  .option(ChannelOption.TCP_NODELAY, true);
	}

	public final NetClientConfig getConfig() {
		return config;
	}
	
	public void start() {
		if (connecting.get()) {
			throw new RuntimeException(config.clientName + " is connecting");
		}
		
		connecting.set(true);
		
		var f = bootStrap.connect(new InetSocketAddress(config.serverIP, config.serverPort));
		Glog.debug("{} do connect to {}:{}", config.clientName, config.serverIP, config.serverPort);
		f.addListener(new ChannelFutureListener() {
	            public void operationComplete(ChannelFuture future) throws Exception {
	                if (future.isSuccess()) {
	                	Glog.debug("{} Connected success", config.clientName);
	                } else {
	                	setChannel(null);
                        Glog.error("{} Connect Exception:Success: "
                                + future.isSuccess() + "  Done: "
                                + future.isDone() + "  Cause: "
                                + future.cause().toString(), config.clientName);
	                }
	                connecting.set(false);
	            }
			}
        );
	}
	
	public void setChannel(ChannelHandlerContext ctx) {
		this.ctx = ctx;
		if(null != this.ctx) {
			this.channel = this.ctx.channel();
		} else {
			this.channel = null;
		}
	}
	
	/**
	 * 关闭
	 * @throws InterruptedException 
	 */
	public void stop() throws InterruptedException {
		connecting.set(false);
		ctx = null;
		
		if(null != channel && channel.isActive()) {
			ChannelFuture future = channel.close();
			future.await();
		}
		channel = null;
		ctx = null;
		bossGroup.shutdownGracefully().sync();
		workerGroup.shutdownGracefully().sync();
		bootStrap = null;
	}
	
	public void writeAndFlush(Message message) {
		writeAndFlush(ctx, message);
	}

	public void writeAndFlush(IObject object, Message message) {
		writeAndFlush(ctx, object, message);
	}
	
	@Override
	public void startup() {
		start();
	}
	
	@Override
	public void close() {
		close();
	}
	
	public ChannelHandlerContext GetChannel() {
		return ctx;
	}

	public void sendRpc(Message arg, Class<? extends Message> resCls, Object param) {
		sendRpc(ctx, arg, resCls, 10, param);
	}
	
	public void sendRpc(Message arg, Class<? extends Message> resCls) {
		sendRpc(ctx, arg, resCls, 10, null);
	}
	
	public void sendRpc(Message arg, Class<? extends Message> resCls, int timeout) {
		sendRpc(ctx, arg, resCls, timeout, null);
	}
	
	public void sendRpc(Message arg, Class<? extends Message> resCls, int timeout, Object param) {
		sendRpc(ctx, arg, resCls, timeout, param);
	}
	
	public void sendRpc(RpcObject rpc) {
		sendRpc(ctx, rpc);
	}
}
