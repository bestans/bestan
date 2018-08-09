package bestan.common.event;

public interface IEventBus<T> {
	void post(T event);
}
