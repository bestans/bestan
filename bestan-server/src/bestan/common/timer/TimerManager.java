package bestan.common.timer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import bestan.common.datastruct.Pair;
import bestan.common.tab.CommonTableDB;
import bestan.common.tab.TableTimer;
import bestan.common.util.Global;
import bestan.log.GLog;

public class TimerManager implements ITimer {
	private static final Logger logger = GLog.log;
	
	/** TimerExcute List */
	private List<TimerList> list;
	
	/** Singleton */
	private static TimerManager instance;
	
	/** Begin Index */
	public static final int TIMER_BEGIN_INDEX = 1;
	
	/** End Index */
	public static final int TIMER_END_INDEX = 2;
	
	/** Loop Index */
	public static final int TIMER_LOOP_INDEX = 3;
	
	/** 没有在Timer.tab 中自定义的TimerID */
	public static final int SPECIAL_TIMER_ID = 12345;
	
	/** server all timer container */
	private Map<Integer, STimer> allTimerMap;
	
	private CommonTableDB tableDB;
	
	/** 
	 * Constructor
	 */
	private TimerManager() {
		this.list = new LinkedList<TimerList>();
		this.allTimerMap = new ConcurrentHashMap<Integer, STimer>();
	}
	
	/**
	 * Get Singleton
	 * @return
	 */
	public static TimerManager getInstance() {
		if(null == instance) {
			instance = new TimerManager();
		}
		
		return instance;
	}

	/**
	 * init Timer.tab
	 */
	public boolean init(CommonTableDB pTableDB) {
		if(null == pTableDB) {
			logger.error("TimerManager init param tableDB is nil");
			return false;
		}
		
		tableDB = pTableDB;
		int num = tableDB.timerConfigTable.rowCount();
		logger.debug("Timer Config Num:" + num);
		long now = DateValidate.getCurrentTimeMillis();
		for(int i = 0; i < num; i++) {
			TableTimer config = tableDB.timerConfigTable.getRowByIndex(i);
			if(null == config) {
				logger.error("get TableTimer Config is null, index:" + i);
				continue;
			}
			
			registerTimerEvetn(this, config.id);
			allTimerMap.put(config.id, getCurrentSTimer(config.id, now));
		}
		
		return true;
	}
	
	/**
	 * add to timer manager
	 * 
	 * @param timestamp
	 * @param timer
	 * @param loopNum
	 * @param subType
	 * @param intervalTime
	 * @param endTime
	 */
	private void add(long timestamp, ITimer timer, int loopNum, int timerid, int subType, int intervalTime, long autoLoopTime, long endTime) {
		int listSize = list.size();
		if(0 == listSize) {
			TimerList newTimerList = new TimerList(timestamp);
			newTimerList.add(new TimerInfo(timer, loopNum, timerid, subType, intervalTime, autoLoopTime, endTime));
			list.add(newTimerList);
			return;
		}
		
		int currentIndex = 0;
		for(currentIndex = 0; currentIndex < listSize; currentIndex++) {
			TimerList entryList = list.get(currentIndex);
			if(timestamp > entryList.getTimeStamp()) {
				continue;
			}
			
			if(timestamp == entryList.getTimeStamp()) {
				entryList.add(new TimerInfo(timer, loopNum, timerid, subType, intervalTime, autoLoopTime, endTime));
				return;
			}
			
			if(timestamp < entryList.getTimeStamp()) {
				TimerList newTimerList = new TimerList(timestamp);
				newTimerList.add(new TimerInfo(timer, loopNum, timerid, subType, intervalTime, autoLoopTime, endTime));
				list.add(currentIndex, newTimerList);
				return;
			}
		}
		
		if(currentIndex == listSize) {
			TimerList newTimerList = new TimerList(timestamp);
			newTimerList.add(new TimerInfo(timer, loopNum, timerid, subType, intervalTime, autoLoopTime, endTime));
			list.add(currentIndex, newTimerList);
		}
	}
	
	/**
	 * add to timermanager 按事件 从小到大有序排列
	 * 
	 * @param timestamp
	 * @param info
	 */
	public void add(long timestamp, TimerInfo info) {
		int listSize = list.size();
		if(0 == listSize) {
			TimerList newTimerList = new TimerList(timestamp);
			newTimerList.add(info);
			list.add(newTimerList);
			return;
		}
		
		int currentIndex = 0;
		for(currentIndex = 0; currentIndex < listSize; currentIndex++) {
			TimerList entryList = list.get(currentIndex);
			if(timestamp > entryList.getTimeStamp()) {
				continue;
			}
			
			if(timestamp == entryList.getTimeStamp()) {
				entryList.add(info);
				return;
			}
			
			if(timestamp < entryList.getTimeStamp()) {
				TimerList newTimerList = new TimerList(timestamp);
				newTimerList.add(info);
				list.add(currentIndex, newTimerList);
				return;
			}
		}
		
		// 添加到尾部
		if(currentIndex == listSize) {
			TimerList newTimerList = new TimerList(timestamp);
			newTimerList.add(info);
			list.add(currentIndex, newTimerList);
		}
	}
	
	/**
	 * Register Timer
	 * 
	 * @param timer
	 * @param timeStamp
	 */
	public void registerTimerEvent(ITimer timer, long timeStamp) {
		add(timeStamp, timer, Global.INVALID_VALUE, SPECIAL_TIMER_ID, TIMER_BEGIN_INDEX, Global.INVALID_VALUE, Global.INVALID_VALUE, Global.INVALID_VALUE);
	}
	
	/**
	 * Register Timer
	 * 
	 * @param timer
	 * @param beginTime
	 * @param endTime
	 * @param loopCount
	 * @param interValSec
	 */
	public void registerTimerEvent(ITimer timer, long timeStamp, int loopCount, int interValSec) {
		add(timeStamp, timer, loopCount, SPECIAL_TIMER_ID, TIMER_BEGIN_INDEX, interValSec, Global.INVALID_VALUE, Global.INVALID_VALUE);
	}
	
	/**
	 * register timer event
	 * @param timer
	 * @param timerid 
	 * @return
	 */
	public void registerTimerEvetn(ITimer timer, int timerid) {
		TableTimer config = tableDB.timerConfigTable.get(timerid);
		if(null == config || null == timer) {
			logger.error("registerTimerEvetn param is invalid, timerid:" + timerid);
			return;
		}
		
		
		long nowTime = DateValidate.getCurrentTimeMillis();
		STimer sTimer = getCurrentSTimer(timerid, nowTime);
		if(null == sTimer) {
			return;
		}
		
		int num = config.beginTimeStr.length;
		for(int i = 0; i < num; i++) {
			if(Global.INVALID_VALUE == sTimer.beginTime[i]) {
				logger.error("registerTimerEvetn timerid:" + timerid + " get begintime is invalid");
				continue;
			}
			
			if(0 < sTimer.endTime[i] && sTimer.endTime[i] < sTimer.beginTime[i]) {
				logger.error("registerTimerEvetn timerid:" + timerid + " endTimer is smaller than begintime");
				continue;
			}
			
			add(sTimer.beginTime[i], timer, config.loopNum, timerid, TIMER_BEGIN_INDEX, config.intervalTime, config.autoLoopMillTime, sTimer.endTime[i]);
			
			if(0 < sTimer.endTime[i]) {
				add(sTimer.endTime[i], timer, config.loopNum, timerid, TIMER_END_INDEX, config.intervalTime, config.autoLoopMillTime, sTimer.endTime[i]);
			}
		}

		return;
	}
	
	/**
	 * Check And Excuten
	 * @Desc 在 主线程中执行
	 * 
	 * @param timestamp
	 */
	public void checkAndExcute(long timestamp) {
		while(!list.isEmpty()) {
			TimerList entryList = list.get(0);
			if(null == entryList || timestamp < entryList.getTimeStamp()) {
				return;
			}
			
			entryList.excute();
			list.remove(0);
		} 
	}
	
	/**
	 * timerId 是否在有效状态
	 * @param timerId
	 * @return
	 */
	public boolean isActive(int timerId) {
		if(!allTimerMap.containsKey(timerId)) {
			return false;
		}
		
		STimer tmVal = allTimerMap.get(timerId);
		if(null == tmVal) {
			return false;
		}
		
		return tmVal.isActive;
	}
	
	/**
	 * 获取开始和结束时间 通过timerId 除非已结束了
	 * @param timerId
	 * @return
	 */
	public Pair<Long, Long> getTime(int timerId) {
		if(!allTimerMap.containsKey(timerId)) {
			return null;
		}
		
		STimer tmVal = allTimerMap.get(timerId);
		if(null == tmVal) {
			return null;
		}
		
		return new Pair<Long, Long>(tmVal.beginTime[0], tmVal.endTime[0]);
	}
	
	/**
	 * timeStamp 在当前的TimerId 有效状态
	 * @param timerId
	 * @param timeStamp
	 * @return
	 */
	public boolean isActive(int timerId, long timeStamp) {
		STimer sTimer = getCurrentSTimer(timerId, DateValidate.getCurrentTimeMillis());
		if(null == sTimer) {
			return false;
		}
		
		int nCount = sTimer.beginTime.length;
		for(int i = 0; i < nCount; i++) {
			if(Global.INVALID_VALUE == sTimer.beginTime[i] || Global.INVALID_VALUE == sTimer.endTime[i]) {
				continue;
			}
			
			if(timeStamp >= sTimer.beginTime[i] && timeStamp <= sTimer.endTime[i]) {
				return true;
			}
		}
		
		return false;
	}
	
	public class STimer {
		public int timerId;
		public long beginTime[];
		public long endTime[];
		public boolean isActive = false;
	}
	/**
	 * 获取最新的timerid 的时间
	 * @warn 这是一个耗时操作 执行一次 大概会用  200 us
	 * @param timerId
	 * @return
	 */
	public STimer getCurrentSTimer(int timerId, long timeStamp) {
		if(Global.INVALID_VALUE == timerId) {
			return null;
		}
		
		TableTimer config = tableDB.timerConfigTable.get(timerId);
		if(null == config) {
			return null;
		}
		
		STimer retTimer = new STimer();
		retTimer.timerId = timerId;
		
		int dayOfWeek = DateValidate.getDayOfWeek(timeStamp);
		int nCount = config.beginTimeStr.length;
		retTimer.beginTime = new long[nCount];
		retTimer.endTime = new long[nCount];
		for(int i = 0; i < nCount; i++) {
			retTimer.beginTime[i] = Global.INVALID_VALUE;
			retTimer.endTime[i] = Global.INVALID_VALUE;
			switch (config.type) {
				case DateValidate.YMDHms:
				{
					// 只执行一次 除已过期的全都添加进结构体中
					long beginTime = DateValidate.parseDate(config.beginTimeStr[i]);
					long endTime = DateValidate.parseDate(config.endTimeStr[i]);
					if(Global.INVALID_VALUE == endTime) {
						if(timeStamp < beginTime) {
							retTimer.beginTime[i] = beginTime;
						} else if(config.intervalTime > 0 && config.loopNum > 0 && timeStamp < (beginTime + config.intervalTime * DateValidate.secondMills * config.loopNum)) {
							retTimer.beginTime[i] = timeStamp;
						}
					} else if(timeStamp < endTime) {
						retTimer.beginTime[i] = beginTime;
						retTimer.endTime[i] = endTime;
					}
				}
					break;
				case DateValidate.Hms: 
				{
					// 每天循环模式 取当时最新的值添加进去
					long beginTime = DateValidate.parseDate(config.beginTimeStr[i]);
					long endTime = DateValidate.parseDate(config.endTimeStr[i]);
					if(Global.INVALID_VALUE == endTime) {
						if(timeStamp < beginTime) {
							retTimer.beginTime[i] = beginTime;
						} else if(config.intervalTime > 0 && config.loopNum > 0 && timeStamp < (beginTime + config.intervalTime * DateValidate.secondMills * config.loopNum)) {
							retTimer.beginTime[i] = timeStamp;
						} else {
							retTimer.beginTime[i] = beginTime + DateValidate.dayMills;
						}
					} else if(timeStamp < endTime) {
						retTimer.beginTime[i] = beginTime - config.deltaDay[i] * DateValidate.dayMills;
						retTimer.endTime[i] = endTime;
					} else {
						retTimer.beginTime[i] = beginTime + (1 - config.deltaDay[i]) * DateValidate.dayMills;
						retTimer.endTime[i] = endTime + DateValidate.dayMills;
					}
				}
					break;
				case DateValidate.WHms:
				{
					// 每周循环模式 取当时最新的值添加进去
					long beginTime = DateValidate.parseDate(config.beginTimeStr[i]) + (config.beginWeekDay[i] - dayOfWeek) * DateValidate.dayMills;
					long endTime = DateValidate.parseDate(config.endTimeStr[i]) + (config.endWeekDay[i] - dayOfWeek) * DateValidate.dayMills;
					if(Global.INVALID_VALUE == endTime) {
						if(timeStamp < beginTime) {
							retTimer.beginTime[i] = beginTime;
						} else if(config.intervalTime > 0 && config.loopNum > 0 && timeStamp < (beginTime + config.intervalTime * DateValidate.secondMills * config.loopNum)) {
							retTimer.beginTime[i] = timeStamp;
						} else {
							retTimer.beginTime[i] = beginTime + DateValidate.weekMills;
						}
					} else if(timeStamp < endTime) {
						retTimer.beginTime[i] = beginTime - config.deltaDay[i] * DateValidate.dayMills;
						retTimer.endTime[i] = endTime;
					} else {
						retTimer.beginTime[i] = beginTime + (7 - config.deltaDay[i]) * DateValidate.dayMills;
						retTimer.endTime[i] = endTime + DateValidate.weekMills;	
					}
				}
				default:
					break;
			}
		}
		
		return retTimer;
	}
	
	public int getTimerRound(int timerId) {
		return getTimerRound(timerId, DateValidate.getCurrentTimeMillis());
	}
	
	/**
	 * 获取当前的round数 (只针对循环的timer才有round值)
	 * eg: 每日任务是从1970.1.1 日起的第几天 + delta值 因为有  20:00:00 ~ 第二天05:00:00 这种情况按开始时间来算
	 *     每周任务是从1970.1.1 日起的第几周 + delta值 因为有  5,20:00:00 ~ 第二周2,05:00:00 这种情况按开始时间来算
	 *     
	 * @param timerId
	 * @param timeStamp
	 * @return
	 */
	public int getTimerRound(int timerId, long timeStamp) {
		TableTimer config = tableDB.timerConfigTable.get(timerId);
		if(null == config) {
			return Global.INVALID_VALUE;
		}
		
		int round = Global.INVALID_VALUE;
		int delta = 0;
		if(DateValidate.YMDHms == config.type) {
			//TODO: do nothing
		} else if(DateValidate.WHms == config.type) {
			round = DateValidate.getCurrentWeek(timeStamp);
			int dayOfWeek = DateValidate.getDayOfWeek(timeStamp);
			for(int i = 0; i < config.beginTimeStr.length; i++) {
				if(0 < config.deltaDay[i] && dayOfWeek < config.endWeekDay[i]) {
					delta = -1;
				}
			}
		} else if(DateValidate.Hms == config.type) {
			round = DateValidate.getCurrentDay(timeStamp);
			for(int i = 0; i < config.beginTimeStr.length; i++) {
				if(0 < config.deltaDay[i]) {
					String timeStampHms = DateValidate.getHHmmssStr(timeStamp);
					if(0 <= timeStampHms.compareTo(DateValidate.HmsZeroStr) && 0 >= timeStampHms.compareTo(config.endTimeStr[i])) {
						delta = -1;
					}
				}
			}
		}
		
		return round + delta;
	}
	
	/**
	 * Get YMDHms 模式下 这个timerid中最大的结束时间
	 * @param timerId
	 * @return
	 */
	public long getMaxEndYMDHmsTimeByTimerId(int timerId) {
		TableTimer config = tableDB.timerConfigTable.get(timerId);
		if(null == config || DateValidate.YMDHms != config.type) {
			return Global.INVALID_VALUE;
		}
		
		long maxValue = Global.INVALID_VALUE;
		for(int i = 0; i < config.endTimeStr.length; i++) {
			long tmpTime = DateValidate.parseDate(config.endTimeStr[i]);
			maxValue = (tmpTime > maxValue) ? tmpTime : maxValue;
		}
		
		return maxValue;
	}
	
	public final Map<Integer, STimer> getTimerMap() {
		return this.allTimerMap;
	}
	
	public void doTimeTrigger(int timerId) {
		if(logger.isDebugEnabled()) {
			logger.debug("TimerManager timerId:" + timerId + " begin.");
		}
		
		if(!allTimerMap.containsKey(timerId)) {
			return;
		}
		
		STimer tmVal = getCurrentSTimer(timerId, DateValidate.getCurrentTimeMillis());
		if(null == tmVal) {
			allTimerMap.remove(timerId);
			return;
		}
		tmVal.isActive = true;
		
		allTimerMap.put(timerId, tmVal);
	}

	public void doTimeLoopTrigger(int timerId, int index) {
		// Do Nothing ...
	}

	public void doTimeUnTrigger(int timerId) {
		if(logger.isDebugEnabled()) {
			logger.debug("TimerManager timerId:" + timerId + " end.");
		}
		
		if(!allTimerMap.containsKey(timerId)) {
			return;
		}
		
		STimer tmVal = getCurrentSTimer(timerId, DateValidate.getCurrentTimeMillis());
		if(null == tmVal) {
			allTimerMap.remove(timerId);
			return;
		}
		tmVal.isActive = false;
		
		allTimerMap.put(timerId, tmVal);
	}
}
