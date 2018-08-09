package bestan.common.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import bestan.common.event.IEvent;
import bestan.common.thread.BThreadPoolExecutors.BRejectedExecutionHandler;
import bestan.common.thread.BThreadPoolExecutors.BThreadFactory;

public class BThreadPoolExecutor implements BExecutor  {
	private List<ThreadPoolExecutor> executors;
	private int executorSize = 0;

	public BThreadPoolExecutor(int nThreadPool) {
		this("pool", nThreadPool);
	}
	
	public BThreadPoolExecutor(String threadPoolName, int nThreadPool) {
		if (nThreadPool <= 0)
			throw new RuntimeException("invalid ThreadPool count(" + nThreadPool + ")");
		
		executors = new ArrayList<>();
		for (int i = 0; i < nThreadPool; ++i) {
			var executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MICROSECONDS,
					new LinkedBlockingDeque<Runnable>(), new BThreadFactory(threadPoolName + "-" + i),
					new BRejectedExecutionHandler());
			executors.add(executor);
		}
		executorSize = nThreadPool;
	}
	
	public ThreadPoolExecutor getExecutor(int index) {
		if (index >= 0 && index < executorSize) {
			return executors.get(index);
		}
		
		return null;
	}
	
	@Override
	public void execute(IEvent event) {
		int index = 0;
		if (event.getID() == 0) {
			index = getIdleExecutorIndex();
		} else {
			index = (int) (event.getID() % executorSize);
		}
		executors.get(index).execute(event);
	}
	
	private int getIdleExecutorIndex() {
		int index = 0;
		int curQueueSize = -1;
		for (int i = 0; i < executorSize; ++i) {
			int tempSize = executors.get(i).getQueue().size();
			if (tempSize <= 0) {
				return i;
			}
			if (tempSize < curQueueSize) {
				index = i;
				curQueueSize = tempSize;
			}
		}
		return index;
	}
}
