package bestan.common.eventbus;

public class EventBusMannager {
	public static void postEvent(IEventBus<Runnable> eventBus, Runnable event) {
		eventBus.post(event);
	}
}
