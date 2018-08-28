package bestan.common.timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import bestan.common.event.IEvent;
import bestan.common.log.Glog;
import bestan.common.logic.ServerConfig;
import bestan.common.module.IModule;
import bestan.common.thread.BExecutor;

public class BTimer {
	private static BExecutor workExecutor;
	private static Multimap<Long, Observer> observers = ArrayListMultimap.create();
	private static Multimap<Long, Observer> newObservers = ArrayListMultimap.create();
	private static ReentrantLock lock = new ReentrantLock();
	private static long tickNow = 0;
	private static ReentrantLock tickLock = new ReentrantLock();
	private static ExecutorService timeExecutor;
	private static boolean stop = false;
	
	private static void attachObserver(Observer ob) {
		if (stop) {
			return;
		}
		lock.lock();
		try {
			newObservers.put(ob.getInterval() + tickNow, ob);
		} finally {
			lock.unlock();
		}
	}
	
	private static void calcTickNow() {
		tickLock.lock();
		try {
			tickNow = System.currentTimeMillis();
		} finally {
			tickLock.unlock();
		}
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
		calcTickNow();
		
		var tempObServers = mergeNewObservers();
		observers.putAll(tempObServers);
		for (var it : observers.entries()) {
			if (tickNow < it.getKey()) {
				break;
			}
			var observer = it.getValue();
			observer.update();
			observers.entries().remove(it);
			if (!observer.isValid()) {
				attachObserver(observer);
			}
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
	
	public static int getTime() {
		return 0;
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
		@Override
		public void startup(ServerConfig config) {
			stop = false;
			workExecutor = config.workExecutor;
			timeExecutor = Executors.newFixedThreadPool(1);
			timeExecutor.execute(new Runnable() {
				@Override
				public void run() {
					while (true) {
						update();
						try {
							Thread.sleep(config.tickInterval);
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
