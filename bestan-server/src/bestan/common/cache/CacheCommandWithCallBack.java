package bestan.common.cache;

import bestan.common.server.LogicServerManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public abstract class CacheCommandWithCallBack extends CacheCommand {
	protected CacheCallBackHandler callback;
	private ChannelHandlerContext ctx;
	
	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 * @param protocolVersion protocolVersion
	 */
	protected CacheCommandWithCallBack(CacheCallBackHandler callback, ChannelHandlerContext ctx) {
		this.callback = callback;
		this.ctx = ctx;
	}

	/**
	 * gets the SocketChannel
	 *
	 * @return SocketChannel
	 */
	public Channel getChannel() {
		return ctx.channel();
	}
	
	/**
	 * get current mode used channel
	 * 
	 * @return Object
	 */
	public ChannelHandlerContext getUseChannel() {
		return ctx;
	}
	
	/**
	 * invokes the callback.
	 */
	public void invokeCallback() {
		if (callback != null) {
			// this way is use logic main thread do player logic
			// 使用接口来实现这个功能
//			LogicServerManager.getInstance().putToMessageQueue(new CacheMessagePack(callback, this));
			LogicServerManager.getInstance().getLogicServer().putToMessageQueue(new CacheMessagePack(callback, this));
		}
	}
}
