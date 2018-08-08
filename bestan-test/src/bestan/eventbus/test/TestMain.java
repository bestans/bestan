package bestan.eventbus.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.AsyncEventBus;

import bestan.common.log.Glog;

public class TestMain {
	public static void test1() {
		var bq = new ArrayBlockingQueue<Runnable>(2);
		var reject = new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
				Glog.debug("reject={},{}", arg0.getClass(), Thread.currentThread().getName());
			}
		};
		var tp = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MICROSECONDS, bq, reject);
		var myRun = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					Glog.debug("myRun={}", Thread.currentThread().getName());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		for (int i = 0; i < 10; ++i)
		{
			tp.execute(myRun);
		}
	}
	public static void test0() {
		var t = new ThreadA();
		var tp = Executors.newSingleThreadExecutor();
		var eventBus = new AsyncEventBus(tp);
		eventBus.register(new Event());
		eventBus.post("aaa");
		eventBus.post("bbb");
	}
	public static void main(String[] args) {
		test1();
	}

}
