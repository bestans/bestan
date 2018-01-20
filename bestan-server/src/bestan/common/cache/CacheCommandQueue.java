package bestan.common.cache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bestan.common.config.RedisConfig;
import bestan.common.util.Global;
import bestan.common.util.MathMatic;

public final class CacheCommandQueue {
	private BlockingQueue<CacheCommand>[] pendingCommands;
	
	/** the DBRun thread`s index */
	private int index;
	
	/** the DBRun thead`s count */
	private int count;
	
	/** the DBBackThread Array */
	private Thread[] aThreads;
	
	/** the flag of finished */
	private volatile boolean finished;

	/** 
     *类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 
     *没有绑定关系，而且只有被调用到时才会装载，从而实现了延迟加载。 
     */  
    private static class CacheCommandQueueHolder{  
        /** 
         * 静态初始化器，由JVM来保证线程安全 
         */  
        private static CacheCommandQueue instance = new CacheCommandQueue();  
    }  
    
	/**
	 * gets the singleton instance
	 *
	 * @return DBCommandQueue
	 */
	public static CacheCommandQueue get() {
		return CacheCommandQueueHolder.instance;
	}

	@SuppressWarnings("unchecked")
	private CacheCommandQueue() {
		count = RedisConfig.getInstance().bgThreadNum;
		index = 0;
		
		pendingCommands = new BlockingQueue[count];
		aThreads = new Thread[count];
		
		for(int i = 0; i < count; i++) {
			this.pendingCommands[i] = new LinkedBlockingQueue<CacheCommand>();
			aThreads[i] = new Thread(new CacheCommandQueueBackgroundThread(i), Global.CACHE_THREAD_NAME_PREF + i);
			aThreads[i].start();
		}
	}

	/**
	 * genrate to index thread to do work be
	 * 现在是单线程所以先不用 synchronized
	 */
	public void generateIndex() {
		index = (index + 1) % count;
	}
	
	/**
	 * enqueues a "fire and forget" command.
	 *
	 * @param command DBCommand to add to the queue
	 */
	public void enqueue(CacheCommand command) {
		if(null == command) {
			return;
		}
		
		generateIndex();
		pendingCommands[index].add(command);
	}
	

	/**
	 * 通过Key 得到对应的 thread index
	 * @param roleId
	 * @return
	 */
	private int getThreadIdxByKey(String key) {
		return MathMatic.abs((int)(key.hashCode() % count));
	}
	
	/**
	 * 通过key 找自己对应的cache thread
	 * @param command
	 * @param key
	 */
	public void enqueue(CacheCommand command, String key) {
		if(null == command) {
			return;
		}
		
		if(null == key || key.isEmpty()) {
			generateIndex();
			pendingCommands[index].add(command);
		} else {
			int idx = getThreadIdxByKey(key);
			pendingCommands[idx].add(command);
		}
	}

	/**
	 * gets the next command in the queue.
	 *
	 * @return next command or <code>null</code>
	 * @throws InterruptedException in case the waiting was interrupted
	 */
	protected CacheCommand getNextCommand(int idx) throws InterruptedException {
		if(idx < 0 || idx >= count) {
			return null;
		}
		
		return pendingCommands[idx].take();
	}

	/**
	 * shuts the background thread down.
	 */
	public void finish() {
		finished = true;
		// force close the cache
		for(int i = 0; i < aThreads.length; ++i) {
			aThreads[i].interrupt();
		}
	}

	/**
	 * should the background set be terminated?
	 *
	 * @return true, if the background thread should be terminated, false if it should continue.
	 */
	protected boolean isFinished() {
		return finished;
	}
}
