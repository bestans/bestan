package bestan.common.db.command;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bestan.common.config.ServerConfig;
import bestan.common.util.Global;
import bestan.common.util.MathMatic;

/**
 * An asynchronous command queue.
 */
public final class DBCommandQueue {
	/** recv from logic thread to excute by DBRun thread */
	private BlockingQueue<DBCommandMetaData>[] pendingCommands;
	
	/** ??? */
	private List<DBCommandMetaData>[] processedCommands;

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
    private static class DBCommandQueueHolder{  
        /** 
         * 静态初始化器，由JVM来保证线程安全 
         */  
        private static DBCommandQueue instance = new DBCommandQueue();  
    }  
    
	/**
	 * gets the singleton instance
	 *
	 * @return DBCommandQueue
	 */
	public static DBCommandQueue get() {
		return DBCommandQueueHolder.instance;
	}

	/**
	 * createsa a new DBCommandQueue
	 */
	@SuppressWarnings("unchecked")
	private DBCommandQueue() {
		count = ServerConfig.getInstance().db_back_thread_size;
		index = 0;
		
		pendingCommands = new BlockingQueue[count];
		processedCommands = new List[count];
		aThreads = new Thread[count];
		
		for(int i = 0; i < count; i++) {
			this.pendingCommands[i] = new LinkedBlockingQueue<DBCommandMetaData>();
			this.processedCommands[i] = Collections.synchronizedList(new LinkedList<DBCommandMetaData>());
			aThreads[i] = new Thread(new DBCommandQueueBackgroundThread(i), Global.DBCOMMAND_THREAD_NAME_PREF + i);
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
	 * 通过roleid 得到对应的 thread index
	 * @param roleId
	 * @return
	 */
	public int getThreadIdxByRoleId(long roleId) {
		return MathMatic.abs((int)(roleId % count));
	}
	
	/**
	 * enqueues a "fire and forget" command.
	 *
	 * @param command DBCommand to add to the queue
	 */
	public void enqueue(DBCommand command) {
		generateIndex();
		pendingCommands[index].add(new DBCommandMetaData(command, null, Thread.currentThread(), false));
	}
	
	/**
	 * enqueues a "fire and forget" command by fix thread 
	 * 
	 * @param command
	 * @param idx
	 */
	public void enqueue(DBCommand command, long roleId) {
		if(Global.INVALID_VALUE == roleId) {
			generateIndex();
			pendingCommands[index].add(new DBCommandMetaData(command, null, Thread.currentThread(), false));
		} else {
			int idx = getThreadIdxByRoleId(roleId);
			pendingCommands[idx].add(new DBCommandMetaData(command, null, Thread.currentThread(), false));
		}
	}

	/**
	 * enqueues a command and remembers the result.
	 *
	 * @param command DBCommand to add to the queue
	 * @param handle ResultHandle
	 */
	public void enqueueAndAwaitResult(DBCommand command, ResultHandle handle) {
		generateIndex();
		pendingCommands[index].add(new DBCommandMetaData(command, handle, Thread.currentThread(), true));
	}

	/**
	 * gets the next command in the queue.
	 *
	 * @return next command or <code>null</code>
	 * @throws InterruptedException in case the waiting was interrupted
	 */
	protected DBCommandMetaData getNextCommand(int idx) throws InterruptedException {
		if(idx < 0 || idx >= count) {
			return null;
		}
		
		//return pendingCommands[idx].poll(1, TimeUnit.MICROSECONDS);
		return pendingCommands[idx].take();
	}

	/**
	 * adds a result to be fetched later
	 *
	 * @param metaData a processed DBCommandMetaData
	 */
	protected void addResult(DBCommandMetaData metaData, int idx) {
		if(idx < 0 || idx >= count) {
			return;
		}
		
		processedCommands[idx].add(metaData);
	}

	/**
	 * gets the processed results of the specified DBCommand class that have
	 * been requested in the current thread.
	 *
	 * @param <T> the type of the DBCommand
	 * @param clazz the type of the DBCommand
	 * @param handle a handle to the expected results
	 * @return a list of processed DBCommands; it may be empty
	 */
	@SuppressWarnings("unchecked")
	public <T extends DBCommand> List<T> getResults(Class<T> clazz, ResultHandle handle, int idx) {
		LinkedList<T> res = new LinkedList<T>();
		if(idx < 0 || idx >= count) {
			return null;
		}
		
		synchronized(processedCommands) {			
			Iterator<DBCommandMetaData> itr = processedCommands[idx].iterator();
			while (itr.hasNext()) {
				DBCommandMetaData metaData = itr.next();
				DBCommand command = metaData.getCommand();
				if (clazz.isAssignableFrom(command.getClass())) {
					if (metaData.getResultHandle() == handle) {
						res.add((T) command);
						itr.remove();
					}
				}
			}
		}
		return res;
	}


	/**
	 * gets one processed result of the specified DBCommand class that have
	 * been requested in the current thread.
	 *
	 * @param <T> the type of the DBCommand
	 * @param clazz the type of the DBCommand
	 * @param handle a handle to the expected results
	 * @return a list of processed DBCommands; it may be empty
	 */
	@SuppressWarnings("unchecked")
	public <T extends DBCommand> T getOneResult(Class<T> clazz, ResultHandle handle, int idx) {
		if(idx < 0 || idx >= count) {
			return null;
		}
		
		synchronized(processedCommands) {
			Iterator<DBCommandMetaData> itr = processedCommands[idx].iterator();
			while (itr.hasNext()) {
				DBCommandMetaData metaData = itr.next();
				DBCommand command = metaData.getCommand();
				if (clazz.isAssignableFrom(command.getClass())) {
					if (metaData.getResultHandle() == handle) {
						itr.remove();
						return (T) command;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 获取当前正在执行队列的大小
	 * TODO: 需要只判断登陆包的大小，因为登陆包耗时并需要特殊处理
	 * 
	 * @param roleid
	 * @return
	 */
	public final int getSizeByRoleId(final long roleid) {
		int idx = getThreadIdxByRoleId(roleid);
		if(Global.INVALID_VALUE == roleid) {
			idx = (index + 1) % count;
		}

		return pendingCommands[idx].size();
	}
	
	/**
	 * 获取最小值
	 * 
	 * @return
	 */
	public final int getMinSize() {
		int min = 0;
		for(int i = 0; i < pendingCommands.length; ++i) {
			int tmpCount = pendingCommands[i].size();
			if(min > tmpCount) {
				min = tmpCount;
			}
		}
		
		return min;
	}

	/**
	 * shuts the background thread down.
	 */
	public void finish() {
		finished = true;
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
