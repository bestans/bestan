package bestan.common.timer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;

import bestan.log.GLog;

public class TimerList implements Iterable<TimerInfo> {
	private static final Logger logger = GLog.log;
	
	private List<TimerInfo> nodeList;
	private long timestamp;
	
	/**
	 * Constructor
	 */
	public TimerList(long timeStamp) {
		this.nodeList = new LinkedList<TimerInfo>();
		
		this.timestamp = timeStamp;
	}
	
	/**
	 * get TimerList size
	 * 
	 * @return nodeList size
	 */
	public int getSize() {
		return nodeList.size();
	}
	
	/**
	 * Add to List
	 * 
	 * @param node TimerInfo
	 */
	public void add(TimerInfo node) {
		nodeList.add(node);
	}
	
	/**
	 * get the register time
	 * 
	 * @return _time int
	 */
	public long getTimeStamp() {
		return this.timestamp;
	}
	
	/**
	 * get List
	 * 
	 * @return nodeList List<TimerInfo>
	 */
	public List<TimerInfo> getList() {
		return nodeList;
	}
	
	/**
	 * This method returns an iterator over the available TimerInfo objects.
	 *
	 * @return the iterator
	 */
	public Iterator<TimerInfo> iterator() {
		return nodeList.listIterator();
	}
	
	/**
	 * clear list
	 */
	public void clear() {
		nodeList.clear();
	}
	
	/**
	 * excute itimer in list
	 */
	public void excute() {
		for(TimerInfo info : nodeList) {
			try {
				switch (info.subType) {
					case TimerManager.TIMER_BEGIN_INDEX: {
						info.timerEvent.doTimeTrigger(info.timerId);
						if(0 < info.autoLoopTime) {
							TimerManager.getInstance().add(this.timestamp + info.autoLoopTime, info);
						}
					}
						break;
					case TimerManager.TIMER_LOOP_INDEX: {
						info.timerEvent.doTimeLoopTrigger(info.timerId, info.loopIndex);
					}
						break;
					case TimerManager.TIMER_END_INDEX: {
						info.timerEvent.doTimeUnTrigger(info.timerId);
						if(0 < info.autoLoopTime) {
							TimerManager.getInstance().add(this.timestamp + info.autoLoopTime, info);
						}
					}
						break;
					default:
						break;
				}// end of switch
			} catch (Exception e) {
				logger.error("TimerList excute has exception, TimerId:" + info.timerId + ", event:" + info.timerEvent.toString() + ".", e);
			}
			
			// 如果是循环的则需要添加
			if(
					TimerManager.TIMER_END_INDEX != info.subType 
					&& info.loopNum > 0
					&& info.intervalTime > 0 
					&& info.endTime < 0
				) {
				info.loopNum--;
				info.loopIndex++;
				info.subType = TimerManager.TIMER_LOOP_INDEX;
				TimerManager.getInstance().add(this.timestamp + info.intervalTime * DateValidate.secondMills, info);
			}
		}
		
		clear();
	}
}
