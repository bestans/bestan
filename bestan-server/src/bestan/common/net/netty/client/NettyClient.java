package bestan.common.net.netty.client;

import java.net.InetSocketAddress;

import org.slf4j.Logger;

import bestan.common.config.ServerConfig;
import bestan.common.net.message.MessageFactory;
import bestan.common.util.Global;
import bestan.log.GLog;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
	private int eType;
	private Bootstrap bootstrap;
	private Channel channel;
	private ChannelHandlerContext ctx;
	private volatile boolean isConnectingLock;
	private EventLoopGroup workerGroup;
	private String ip;
	private int port;

	private final static Logger logger = GLog.log;
	public NettyClient(int eType, String ip, int port) {
		this.eType = eType;
		this.ip = ip;
		this.port = port;
		cleanUp();
	}
	
	private void cleanUp() {
		this.bootstrap = null;
		this.channel = null;
		this.ctx = null;
		this.workerGroup = null;
		this.isConnectingLock = false;
	}
	
	public int getType() {
		return eType;
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public void start() {
		// 根据 eType 获取到 ip 和 port
		workerGroup = new NioEventLoopGroup(1);
		this.bootstrap = new Bootstrap();
		this.bootstrap.group(workerGroup)
		    	      .channel(NioSocketChannel.class)
			    	  .handler(new NettyClientInitializer(this))
			    	  .option(ChannelOption.TCP_NODELAY, true)
			          .option(ChannelOption.SO_LINGER, 0);
		
	}
	
	public void connect() {
		ChannelFuture f = this.bootstrap.connect(new InetSocketAddress(ip, port));
		this.isConnectingLock = true;
		
		if(logger.isDebugEnabled()) {
			logger.debug("do connect");
		}
		f.addListener(new ChannelFutureListener() {
	            public void operationComplete(ChannelFuture future) throws Exception {
	                if (future.isSuccess()) {
	                	if(logger.isDebugEnabled()) {
	                		logger.debug("F: Connected:" + future.isDone());
	                	}
	                } else {
	                	setChannel(null);
	                    if (future.isCancelled()) {
	                        logger.warn("Request Cancelled");
	                    } else {
	                        logger.warn("Connect Exception:Success: "
	                                + future.isSuccess() + "  Done: "
	                                + future.isDone() + "  Cause: "
	                                + future.cause().toString());
	                    }
	                }
	                
	                // 清除 连接逻辑锁
	                unLock();
	            }
			}
        );
	}
	
	public void unLock() {
		this.isConnectingLock = false;
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
	 * 发送 注册包
	 */
	public void sendRegister() {
//		SSRegister.Builder builder = SSRegister.newBuilder();
//		builder.setEType(int.GS);
//		builder.setZoneid(ServerConfig.getInstance().zoneid);
//		sendMessage(builder.build());
	}
	
	public void sendMessage(GeneratedMessage msg) {
		if(null == msg || null == ctx) {
			return;
		}
		
		ctx.writeAndFlush(msg);
	}
	
	/**
	 * 是否有效
	 * @return
	 */
	public boolean isTickValid() {
		return null == this.channel && isConnectingLock == false;
	}
	
	public boolean isValid() {
		return null != this.channel && this.channel.isActive();
	}
	
	/**
	 * 关闭
	 */
	public void close() {
		this.isConnectingLock = true;
		this.ctx = null;
		
		if(null != this.channel && this.channel.isActive()) {
			ChannelFuture future = this.channel.close();
			try {
				future.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		this.channel = null;
		try {
			this.workerGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.bootstrap = null;
	}
	
	/**
	 * 发送 通过WorldServer 转发给另一个GS的包
	 * 
	 * @param roleid
	 * @param msg
	 */
	public void sendSSConvertMessage(final long roleid, Message msg) {
//		if(null == msg || Global.INVALID_VALUE == roleid) {
//			logger.error("sendWCConvertMsg param is nil");
//			return;
//		}
//		
//		SSConvertData.Builder convertMsg = SSConvertData.newBuilder();
//		final int msgId = MessageFactory.getFactory().getMsgIdByMessageClass(msg.getClass());
//		if(Global.INVALID_VALUE == msgId) {
//			logger.error("sendSSConvertMsg get msgid is nil, class:" + msg.getClass().toString());
//			return;
//		}
//		convertMsg.setMsgId(msgId);
//		convertMsg.setMsg(msg.toByteString());
//		convertMsg.setRoleid(roleid);
//		
//		ctx.writeAndFlush(convertMsg.build());
	}
	

}
