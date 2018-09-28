package bestan.test.thread;

import bestan.common.thread.BThreadPoolExecutors;

/**
 * @author yeyouhuan
 *
 */
public class ThreadUnit {
	public static void createUnit(ThreadEventFactory factory, int count) {
		for (int i = 0; i < count; ++i) {
			var executor = BThreadPoolExecutors.newFixedThreadPool("thread-" + i, 1);
			executor.execute(factory.createRunable());
		}
	}
}
