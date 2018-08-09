package bestan.common.eventbus;

public interface IEventBus<T> {
	void post(T event);
}
