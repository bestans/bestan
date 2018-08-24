package bestan.common.net;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Maps;

import bestan.common.logic.BaseManager;
import bestan.common.timer.ITimer;
import bestan.common.timer.Timer;

/**
 * @author yeyouhuan
 *
 */
public class RpcManager extends BaseManager implements ITimer {
	private Map<Integer, RpcObject> map = Maps.newHashMap();
	private ReentrantLock lock = new ReentrantLock();
	private AtomicInteger index = new AtomicInteger(1);
	
	private static class RpcManagerHolder {
		private static RpcManager INSTANCE = new RpcManager();
	}
	
	private RpcManager() {
		Timer.attach(this, 100);
	}
	
	public int getAndIncrementIndex() {
		return index.getAndIncrement();
	}
	
	public static RpcManager getInstance() {
		return RpcManagerHolder.INSTANCE;
	}
	
	public void put(AbstractRpc rpc) {
		lock.lock();
		try {
			map.put(rpc.getRpcIndex(), new RpcObject(rpc));
		} finally {
			lock.unlock();
		}
	}
	
	public AbstractRpc get(int index) {
		lock.lock();
		try {
			var rpcObj = map.get(index);
			return rpcObj != null ? rpcObj.getRpc() : null;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void Tick() {
		lock.lock();
		try {
			var curTime = Timer.getTime();
			for (var it : map.entrySet()) {
				if (curTime >= it.getValue().getEndTime()) {
					map.entrySet().remove(it);	
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	private static class RpcObject {
		private AbstractRpc rpc;
		private long endTime;
		
		private RpcObject(AbstractRpc rpc) {
			this.rpc = rpc;
			endTime = Timer.getTime() + rpc.getTimeout();
		}
		public long getEndTime() {
			return endTime;
		}
		public AbstractRpc getRpc() {
			return rpc;
		}
	}
}

