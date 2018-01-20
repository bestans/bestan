package bestan.common.cache;

public interface CacheCallBackHandler {
	/**
	 * handles an delayed event
	 */
	public void handleCacheEvent(Object data);
}
