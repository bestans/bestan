package bestan.common.event;

public interface IEvent extends Runnable {
	default long getID() {
		return 0;
	}
}
