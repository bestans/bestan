package bestan.common.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Strings;

import bestan.common.event.IEvent;
import bestan.common.log.Glog;

public class BThreadPoolExecutors {
	/**
	 * 创建一组单线程池
	 * @param threadPoolName 线程池名字
	 * @param nThread 线程数量
	 * @return 线程池列表
	 */
	public static BThreadPoolExecutor newMutipleSingleThreadPool(String threadPoolName, int nThread){
		return new BThreadPoolExecutor(threadPoolName, nThread);
	}

	/**
	 * 创建一组单线程池
	 * @param nThread 线程数量
	 * @return 线程池列表
	 */
	public static BThreadPoolExecutor newMutipleSingleThreadPool(int nThread){
		return new BThreadPoolExecutor(nThread);
	}
	
	/**
	 * 创建一个固定线程数量的线程池
	 * @param threadPoolName 线程池名字
	 * @param nThread 线程数量
	 * @return 一个固定线程数量的线程池
	 */
	public static Executor newFixedThreadPool(String threadPoolName, int nThread) {
		Executor executor = new ThreadPoolExecutor(nThread, nThread, 0, TimeUnit.MICROSECONDS,
				new LinkedBlockingDeque<Runnable>(), new BThreadFactory(threadPoolName),
				new BRejectedExecutionHandler());
		
		return executor;
	}
	
	public static BExecutor newDirectExecutor() {
		return new BDirectExecutor();
	}
	
	static class BRejectedExecutionHandler implements RejectedExecutionHandler{
		@Override
		public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
			Glog.error("rejectedExecution={},{}", arg0, Thread.currentThread().getName());
		}
	}
	
    static class BThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        BThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (Strings.isNullOrEmpty(name)) {
                name = "pool";
            }

            namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
    
    static class BDirectExecutor implements BExecutor {
		@Override
		public void execute(IEvent event) {
			event.run();
		}
    }
}
