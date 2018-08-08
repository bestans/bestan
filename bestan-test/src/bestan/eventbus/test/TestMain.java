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

import bestan.common.log.Glog;

public class TestMain {
	public static void test1() {
		List<ThreadA> players = new ArrayList<>();
		
		int count = 10;
		for (int i = 0; i < count; ++i) {
			players.add(new ThreadA(new Player()));
		}
		
		var bq = new LinkedBlockingDeque<Runnable>();
		var reject = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
				Glog.debug("reject={},{}", arg0.getClass(), Thread.currentThread().getName());
			}
		};
		var tp = new ThreadPoolExecutor(10, 10, 0, TimeUnit.MICROSECONDS, bq, reject);
		var rand = new Random();
		for (int i = 0; i < 1000000; ++i)
		{
			int index = rand.nextInt(count);
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
	public static void test2() {
		List<ThreadA> players = new ArrayList<>();
		
		int count = 10;
		for (int i = 0; i < count; ++i) {
			players.add(new ThreadA(new Player()));
		}
		
		var reject = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
				Glog.debug("reject={},{}", arg0.getClass(), Thread.currentThread().getName());
			}
		};
		List<ThreadPoolExecutor> tpList = new ArrayList<>();
		for (int i = 0; i < 10; ++i) {
			tpList.add(new ThreadPoolExecutor(1, 1, 0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>(), reject))	;
		}
		var rand = new Random();
		for (int i = 0; i < 1000000; ++i)
		{
			int index = rand.nextInt(count);
			tpList.get(index % 10).execute(players.get(index));
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
	}
	public static void test0() {
		var tp = Executors.newSingleThreadExecutor();
		var eventBus = new AsyncEventBus(tp);
		eventBus.register(new Event());
		eventBus.post("aaa");
		eventBus.post("bbb");
	}
	public static void main(String[] args) {
		test2();
	}

}
