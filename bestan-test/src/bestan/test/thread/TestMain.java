package bestan.test.thread;

import java.util.concurrent.Executors;

import bestan.common.log.Glog;

public class TestMain {
	private static boolean bvalue = false;
	private static int ivalue = 0;
	private static int t1 = 0;
	private static int t2 = 0;
	public static void main(String[] args) {
		Executors.newFixedThreadPool(1).execute(new Runnable() {
			
			@Override
			public void run() {
				while (t1++ < 10000) {
					bvalue = bvalue ? false : true;
					ivalue = t1 * 100000;
				}
			}
		});
		

		Executors.newFixedThreadPool(1).execute(new Runnable() {
			@Override
			public void run() {
				while (t2++ < 10000) {
					bvalue = bvalue ? false : true;
					ivalue = t2 * 10000;
				}
			}
		});
		
		Glog.debug("v={}, v2={}", bvalue, ivalue);
	}

}
