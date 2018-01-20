package bestan.common.net.message;

/**
 * A handler for delayed events.
 */
public interface DelayedEventHandler {

	/**
	 * handles an delayed event
	 */
	public void handleDelayedEvent(Object data);
}
