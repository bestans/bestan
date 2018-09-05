package bestan.common.net;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Maps;
import com.google.protobuf.Message;

import bestan.common.logic.BaseManager;
import bestan.common.protobuf.Proto;
import bestan.common.timer.BTimer;
import bestan.common.timer.ITimer;

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
		BTimer.attach(this, 100);
	}
	
	public int getAndIncrementIndex() {
		return index.getAndIncrement();
	}
	
	public static RpcManager getInstance() {
		return RpcManagerHolder.INSTANCE;
	}
	
	public void put(Proto.RpcMessage.Builder rpc, Message arg, Object param, int timeout) {
		put(new RpcObject(rpc, arg, param, timeout));
	}
	
	public void put(RpcObject rpc) {
		lock.lock();
		try
		{
			rpc.resetEndtime();
			map.put(rpc.getRpcMessage().getRpcIndex(), rpc);
		} finally {
			lock.unlock();
		}
	}
	
	public RpcObject get(int index) {
		lock.lock();
		try {
			return map.get(index);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void Tick() {
		lock.lock();
		try {
			var curTime = BTimer.getTime();
			var it = map.entrySet().iterator();
			while (it.hasNext()) {
				var entry = it.next();
				if (curTime >= entry.getValue().getEndTime()) {
					it.remove();
				}
			}
		} finally {
			lock.unlock();
		}
	}
	
	public static class RpcObject {
		private Proto.RpcMessage.Builder rpc;
		private Message arg;
		private long endTime;
		private Object param;
		private int timeout;
		
		private RpcObject(Proto.RpcMessage.Builder rpc, Message arg, Object param, int timeout) {
			this.rpc = rpc;
			this.param = param;
			this.arg = arg;
			this.timeout = timeout;
		}
		private void resetEndtime() {
			endTime = BTimer.getTime() + timeout;
		}
		public long getEndTime() {
			return endTime;
		}
		public Proto.RpcMessage.Builder getRpcMessage() {
			return rpc;
		}
		public Object getParam() {
			return param;
		}
		public Message getArgMessage() {
			return arg;
		}
	}
}

