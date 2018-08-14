package bestan.common.logic;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import bestan.common.guid.Guid;
import bestan.common.timer.ITimer;

public abstract class BaseObject implements IObject, ITimer {
	private AtomicBoolean isTimerValid = new AtomicBoolean(true);
	private ReentrantLock lock = new ReentrantLock();
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

	@Override
	public boolean validTimer() {
		return isTimerValid.get();
	}
	
	public void setTimerInvalid() {
		isTimerValid.set(false);
	}
}
