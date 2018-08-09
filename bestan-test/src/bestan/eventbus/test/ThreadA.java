package bestan.eventbus.test;

import bestan.common.eventbus.IEvent;

public class ThreadA implements IEvent {
	private Player player;
	
	public ThreadA(Player player) {
		this.player = player;
	}
	
	@Override
	public long getID() {
		return player.id;
	}
	
	public int getCount() {
		return player.getCount();
	}
	@Override
	public void run() {
		player.lock.lock();
//		try {
//			Thread.sleep(1);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		player.CalcCount();
		player.lock.unlock();
	}
}
