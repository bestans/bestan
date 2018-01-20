package bestan.common.cache;
import bestan.common.net.message.IMessagePack;

public class CacheMessagePack implements IMessagePack {
	private final CacheCallBackHandler callBackHandler;
	private final CacheCommandWithCallBack cacheCommand;
	
	
	/**
	 * @return the delayedEventHandler
	 */
	public CacheCallBackHandler getCallBackHandler() {
		return callBackHandler;
	}

	/**
	 * @return the dbCommand
	 */
	public CacheCommandWithCallBack getCommand() {
		return cacheCommand;
	}

	/**
	 * Constructor
	 * 
	 * @param handler
	 * @param data
	 */
	public CacheMessagePack(CacheCallBackHandler handler, CacheCommandWithCallBack data) {
		this.callBackHandler = handler;
		this.cacheCommand = data;
	}
}
