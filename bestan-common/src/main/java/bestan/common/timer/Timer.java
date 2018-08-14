package bestan.common.timer;

import java.util.concurrent.Executor;
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

public class Timer implements IModule {
	private static BExecutor workExecutor;
	private static Multimap<Long, Observer> observers = ArrayListMultimap.create();
	private static Multimap<Long, Observer> newObservers = ArrayListMultimap.create();
	private static ReentrantLock lock = new ReentrantLock();
	private static long tickNow = 0;
	private static Executor timeExecutor = Executors.newFixedThreadPool(1);
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
	
	public void update() {
		calcTickNow();
		
		var tempObServers = mergeNewObservers();
		observers.putAll(tempObServers);
		for (var it : observers.entries()) {
			observers.entries().remove(it);
			attachObserver(it.getValue());
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
	
	private static class TimerObserver extends Observer {
		private ITimer object;
		
		public TimerObserver(ITimer object) {
			this.object = object;
		}

		public boolean update() {
			workExecutor.execute(new TickEvent(object));
			return false;
		}
	}
	
	public static abstract class Observer {
		private AtomicBoolean valid = new AtomicBoolean(true);
		private int interval = 0;
		
		public abstract boolean update();
		public int getInterval() {
			return interval; 
		}
		
		public boolean isValid() {
			return valid.get();
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
		public boolean update() {
			workExecutor.execute(event);
			return false;
		}
	}

	@Override
	public void startup(ServerConfig config) {
		stop = false;
		timeExecutor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					update();
					try {
						Thread.sleep(config.tickInterval);
					} catch (InterruptedException e) {
						Glog.error("timeExecutor run error({})", e.getMessage());
						timeExecutor.execute(this);
						break;
					}
				}
			}
		});
		workExecutor = config.workExecutor;
	}
	
	@Override
	public void close() {
		stop = false;
		
		int maxTimes = 10;
		while ((maxTimes-- > 0) && (observers.size() > 0 || newObservers.size() > 0)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Glog.error("timer close error({})", e.getMessage());
			}
		}
	}
}
