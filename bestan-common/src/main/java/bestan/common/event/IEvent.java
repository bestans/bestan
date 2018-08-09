package bestan.common.event;

public interface IEvent extends Runnable {
	/**
	 * @return 该id用来判断将event放在哪个线程执行，如果为0，则选择一个空闲线程
	 */
	default long getID() {
		return 0;
	}
}
