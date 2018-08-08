package bestan.eventbus.test;

import java.util.concurrent.locks.ReentrantLock;

public class Player {
	public ReentrantLock lock = new ReentrantLock();
	private int count = 0;
	
	public void CalcCount() {
		float ftotal  = 0;
		for (int i = 0; i < 10000; ++i) {
			ftotal += Math.sqrt(i);
		}
		int total = ((int)ftotal) % 2;
		count += (total == 0 ? 1 : total);
	}
	
	public int getCount() {
		return count;
	}
}
