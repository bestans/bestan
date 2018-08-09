package bestan.common.eventbus;

import java.util.concurrent.locks.ReentrantLock;

import com.google.protobuf.Message;

public abstract class BasePlayer implements IEvent {
	private ReentrantLock lock = new ReentrantLock();

	public void lock() {
		lock.lock();
	}
	
	public void unlock() {
		lock.unlock();
	}
	
	public void processCommand(Message message) {
		lock();
		try {
			handleCommand(message);
		} finally {
			unlock();
		}
	}
	
	public abstract void handleCommand(Message message);
}
