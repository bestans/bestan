package bestan.test.thread;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author yeyouhuan
 *
 */
public class ReadWrite {
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	int value = 10;
	
	public int read() {
		lock.readLock().lock();
		try {
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return value;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public void write() {
		lock.writeLock().lock();
		try {

			try {
				Thread.sleep(1000);
				value++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
}
