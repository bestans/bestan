package bestan.common.timer;

public interface ITimer {
	/** 
	 * Timer Begin
	 * 
	 * @param timerId
	 */
	public void doTimeTrigger(int timerId);
	
	/** 
	 * Timer Loop
	 * 
	 * @param timerId
	 */
	public void doTimeLoopTrigger(int timerId, int index);
	
	/**
	 * Timer End
	 * 
	 * @param timerId
	 */
	public void doTimeUnTrigger(int timerId);
}
