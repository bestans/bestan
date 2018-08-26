package bestan.eventbus.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.AsyncEventBus;

import bestan.common.event.BEventBus;
import bestan.common.event.IEvent;
import bestan.common.log.Glog;
import bestan.common.thread.BThreadPoolExecutors;

public class TestMain {
	private static List<ThreadA> getPlayers(int count){
		List<ThreadA> players = new ArrayList<>();
		var rand = new Random();
		for (int i = 0; i < count; ++i) {
			int index = rand.nextInt(100) > 50 ? i : 0;
			players.add(new ThreadA(new Player(index)));
		}
		return players;
	}
	public static void test1(int playerCount, int nThread) {
		var players = getPlayers(playerCount);
		
		var bq = new LinkedBlockingDeque<Runnable>();
		var reject = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
				Glog.debug("reject={},{}", arg0.getClass(), Thread.currentThread().getName());
			}
		};
		var tp = new ThreadPoolExecutor(nThread, nThread, 0, TimeUnit.MICROSECONDS, bq, reject);
		var rand = new Random();
		for (int i = 0; i < 100000; ++i)
		{
			int index = rand.nextInt(playerCount);
			for (int j = 0; j < 10; ++j)
				tp.execute(players.get(index));
		}
		var start = System.currentTimeMillis();
		Glog.debug("calc={},{},{}", tp.getQueue().size(), tp.getCompletedTaskCount(), tp.getTaskCount());
		while (tp.getQueue().size() > 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		Glog.debug("total={},{}", System.currentTimeMillis() - start, tp.getCompletedTaskCount());
		Glog.debug("calc={},{},{}", tp.getQueue().size(), tp.getCompletedTaskCount(), tp.getTaskCount());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int total = 0;
		for (int i = 0; i < players.size(); ++i) {
			total += players.get(i).getCount();
		}
		Glog.debug("total count={}", total);
	}
	public static void test2(int count, int tCount) {
		List<ThreadA> players = getPlayers(count);
		
		for (int i = 0; i < count; ++i) {
			players.add(new ThreadA(new Player(i)));
		}
		
		var reject = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
				Glog.debug("reject={},{}", arg0.getClass(), Thread.currentThread().getName());
			}
		};
		List<ThreadPoolExecutor> tpList = new ArrayList<>();
		for (int i = 0; i < tCount; ++i) {
			tpList.add(new ThreadPoolExecutor(1, 1, 0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>(), reject))	;
		}
		var rand = new Random();
		for (int i = 0; i < 100000; ++i)
		{
			int index = rand.nextInt(count);
			for (int j = 0; j < 10; ++j)
				tpList.get(index % tCount).execute(players.get(index));
		}
		var start = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			boolean end = true;
			for (int i = 0; i < tpList.size(); ++i) {
				if (tpList.get(i).getQueue().size() > 0)
				{
					end = false;
					break;
				}
			}
			if (end) {
				break;
			}
		}
		Glog.debug("total={}", System.currentTimeMillis() - start);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int total = 0;
		for (int i = 0; i < players.size(); ++i) {
			total += players.get(i).getCount();
		}
		Glog.debug("total count={}", total);
		for (var it : tpList) {
			Glog.debug("completed size={}", it.getCompletedTaskCount());
		}
	}
	
	public static void test3(int playerCount, int nThread) { 
		var players = getPlayers(playerCount);
		
		var reject = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
				Glog.debug("reject={},{}", arg0.getClass(), Thread.currentThread().getName());
			}
		};
		var tp = BThreadPoolExecutors.newMutipleSingleThreadPool("mutiple", nThread);
		var rand = new Random();
		for (int i = 0; i < 100000; ++i)
		{
			int index = rand.nextInt(playerCount);
			for (int j = 0; j < 10; ++j)
				tp.execute(players.get(index));
		}
		var start = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			boolean end = true;
			for (int i = 0; i < nThread; ++i) {
				if (tp.getExecutor(i).getQueue().size() > 0)
				{
					end = false;
					break;
				}
			}
			if (end) {
				break;
			}
		}
		Glog.debug("total={}", System.currentTimeMillis() - start);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int total = 0;
		for (int i = 0; i < players.size(); ++i) {
			total += players.get(i).getCount();
		}
		Glog.debug("total count={}", total);

		for (int i = 0; i < nThread; ++i) {
			Glog.debug("size={}", tp.getExecutor(i).getCompletedTaskCount());
		}
	}
	public static void test0() {
		var tp = Executors.newSingleThreadExecutor();
		var eventBus = new AsyncEventBus(tp);
		eventBus.register(new Event());
		eventBus.post("aaa");
		eventBus.post("bbb");
	}
	public static void test10() {
		int nThread = 10;
		int playerCount = 200;
		BEventBus<IEvent> bus = new BEventBus<>(BThreadPoolExecutors.newMutipleSingleThreadPool("test", nThread));
		var players = getPlayers(playerCount);

		var rand = new Random();
		for (int i = 0; i < 100000; ++i) {
			var player = players.get(rand.nextInt(playerCount));
			bus.post(player);
		}
	}
	public static void main(String[] args) {
		int playerCount = 200;
		int nThread = 10;
		test2(playerCount, nThread);
	}

}
