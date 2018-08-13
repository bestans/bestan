package bestan.concurrent.test;

import java.util.concurrent.Executors;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import bestan.common.log.Glog;

public class TestConcurrentMain {

	public static void main(String[] args) {
		var callback = new ITestCallback<String>() {
			@Override
			public String call() {
				// TODO Auto-generated method stub
				//throw new RuntimeException("aaa");
				Glog.debug("call={}", Thread.currentThread().getName());
				return "call";
			}
			
			@Override
			public void onSuccess(String result) {
				Glog.debug("onSuccess={},{}", result, Thread.currentThread().getName());
			}

			@Override
			public void onFailure(Throwable t) {
				Glog.debug("onFailure={},{}", t, Thread.currentThread().getName());
			}
		};
		Glog.debug("main={}", Thread.currentThread().getName());
		var service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
		Futures.addCallback(service.submit(callback), callback, service);
	}

}
