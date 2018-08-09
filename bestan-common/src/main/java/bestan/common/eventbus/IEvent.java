package bestan.common.eventbus;

public interface IEvent extends Runnable {
	default long getID() {
		return 0;
	}
}
