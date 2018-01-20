package bestan.common.db.command;

import bestan.common.net.message.DBMessagePack;
import bestan.common.net.message.DelayedEventHandler;
import bestan.common.server.LogicServerManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * A database command with callback support.
 */
public abstract class DBCommandWithCallback extends AbstractDBCommand {	
	/** a handler that will be informed about the result */
	protected DelayedEventHandler callback;
	
	// net mode netty
	private ChannelHandlerContext ctx;

	
	/**
	 * Creates a new LoadCharacterCommand
	 */
	protected DBCommandWithCallback() {
		// default constructor
	}
	
	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 * @param protocolVersion protocolVersion
	 */
	protected DBCommandWithCallback(DelayedEventHandler callback, ChannelHandlerContext ctx) {
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
	 * get ChannelHandlerContext
	 * 
	 * @return ChannelHandlerContext
	 */
	public ChannelHandlerContext getChannelHandlerContext() {
		return this.ctx;
	}

	/**
	 * invokes the callback.
	 */
	public void invokeCallback() {
		if (callback != null) {
			// this way is use logic main thread do player logic
//			LogicServerManager.getInstance().putToMessageQueue(new DBMessagePack(callback, this));
			LogicServerManager.getInstance().getLogicServer().putToMessageQueue(new DBMessagePack(callback, this));
		}
	}

}
