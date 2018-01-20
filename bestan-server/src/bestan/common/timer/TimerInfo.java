package bestan.common.timer;

public class TimerInfo {
	public ITimer timerEvent;
	public int loopNum;
	public int timerId;
	public int subType;
	public long autoLoopTime;
	public int intervalTime;
	public int loopIndex;
	public long endTime;
	
	public TimerInfo(ITimer iTimer, int loopNum, int timerId, int subType, int intervalTime, long autoLoopTime, long endTime) {
		this.timerEvent = iTimer;
		this.loopNum = loopNum;
		this.timerId = timerId;
		this.subType = subType;
		this.intervalTime = intervalTime;
		this.autoLoopTime = autoLoopTime;
		this.endTime = endTime;
	}
}
