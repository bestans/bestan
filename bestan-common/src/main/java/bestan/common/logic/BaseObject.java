package bestan.common.logic;

import java.util.concurrent.locks.ReentrantLock;

import bestan.common.guid.Guid;

public abstract class BaseObject implements IObject {
	protected ReentrantLock lock = new ReentrantLock();
	protected Guid guid;

	public BaseObject(Guid guid) {
		this.guid = guid;
	}
	
	@Override
	public Guid getGuid() {
		return guid;
	}

	public void lockObject() {
		lock.lock();
	}
	
	public void unlockObject() {
		lock.unlock();
	}
	

}
