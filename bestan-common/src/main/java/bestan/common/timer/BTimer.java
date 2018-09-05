package bestan.common.timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import bestan.common.event.IEvent;
import bestan.common.log.Glog;
import bestan.common.module.IModule;
import bestan.common.thread.BExecutor;

public class BTimer {
	private static BExecutor workExecutor;
	private static Multimap<Long, Observer> observers = ArrayListMultimap.create();
	private static Multimap<Long, Observer> newObservers = ArrayListMultimap.create();
	private static ReentrantLock lock = new ReentrantLock();
	private static ExecutorService timeExecutor;
	private static boolean stop = false;
	private static AtomicLong timeNow = new AtomicLong();
	
	private static void attachObserver(Observer ob) {
		if (stop) {
			return;
		}
		lock.lock();
		try {
			newObservers.put(ob.getInterval() + timeNow.get(), ob);
		} finally {
			lock.unlock();
		}
	}
	
	private static void calcTimeNow() {
		timeNow.set(System.currentTimeMillis());
	}
	
	private static Multimap<Long, Observer> mergeNewObservers() {
		Multimap<Long, Observer> tempObServers = null;
		lock.lock();
		try {
			tempObServers = newObservers;
			newObservers = ArrayListMultimap.create();
		} finally {
			lock.unlock();
		}
		return tempObServers;
	}
	
	private static void update() {
		calcTimeNow();
		
		var tempObServers = mergeNewObservers();
		observers.putAll(tempObServers);
		var tickNow = timeNow.get();
		var it = observers.entries().iterator();
		while (it.hasNext()) {
			var entry = it.next();
			if (tickNow < entry.getKey()) {
				break;
			}
			var observer = entry.getValue();
			observer.update();
			if (observer.isValid()) {
				attachObserver(observer);
			}
			it.remove();
		}
	}
	
	public static void schedule(IEvent event, int delay) {
		if (delay < 0) return;
		
		attach(new TimerTask(event), delay);
	}
	
	public static void attach(Observer ob, int delay) {
		delay = delay > 0 ? delay : 1;
		ob.interval = delay;
		
		attachObserver(ob);
	}
	
	public static long getTime() {
		return timeNow.get() / 1000;
	}
	
	public static long getTimeMillis() {
		return timeNow.get();
	}
	
	public static void attach(ITimer timerObject, int delay) {
		attach(new TimerObserver(timerObject), delay);
	}
	
	private static class TimerObserver extends Observer {
		public TimerObserver(ITimer timerObject) {
			super(null);
			this.timerObject = timerObject;
		}

		public void update() {
			workExecutor.execute(new TickEvent(timerObject));
		}
		
		@Override
		public boolean isValid() {
			return timerObject.validTimer();
		}
	}
	
	public static abstract class Observer {
		private AtomicBoolean valid;
		private int interval = 0;
		protected ITimer timerObject = null;
		
		public Observer() {
			 valid = new AtomicBoolean(true);
		}
		
		public Observer(AtomicBoolean valid) {
			this.valid = valid;
		}
		
		public abstract void update();
		public int getInterval() {
			return interval; 
		}
		
		public boolean isValid() {
			return valid.get();
		}
		public void setInvalid() {
			valid.set(false);
		}
	}
	
	private static class TickEvent implements IEvent{
		private ITimer object;

		public TickEvent(ITimer object) {
			this.object = object;
		}

		@Override
		public void run() {
			object.executeTick();
		}
	}
	
	private static class TimerTask extends Observer {
		private IEvent event;
		
		public TimerTask(IEvent event) {
			this.event = event;
		}
		
		@Override
		public void update() {
			workExecutor.execute(event);
			setInvalid();
		}
	}

	public static class TimerModule implements IModule {
		private BExecutor executor;
		private int timerIickInterval;
		
		/**
		 * @param executor 工作线程
		 * @param timerIickInterval timer更新间隔时间（微秒）
		 */
		public TimerModule(BExecutor executor, int timerIickInterval) {
			this.executor = executor;
			this.timerIickInterval = timerIickInterval;
		}
		
		@Override
		public void startup() {
			stop = false;
			calcTimeNow();
			workExecutor = executor;
			timeExecutor = Executors.newFixedThreadPool(1);
			timeExecutor.execute(new Runnable() {
				@Override
				public void run() {
					while (true) {
						update();
						try {
							Thread.sleep(timerIickInterval);
						} catch (InterruptedException e) {
							Glog.info("timeExecutor run error, maybe close({})", e.getMessage());
							timeExecutor.execute(this);
							break;
						}
					}
				}
			});
		}
		
		@Override
		public void close() {
			stop = true;
			
			int maxTimes = 100;
			while ((maxTimes-- > 0) && (observers.size() > 0 || newObservers.size() > 0)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Glog.error("timer close error({})", e.getMessage());
				}
			}
			timeExecutor.shutdownNow();
		}
	}
}
