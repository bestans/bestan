package bestan.eventbus.test;

public class ThreadA extends Thread {
	private Player player;
	
	public ThreadA(Player player) {
		this.player = player;
	}
	
	public int getCount() {
		return player.getCount();
	}
	@Override
	public void run() {
		//player.lock.lock();
//		try {
//			Thread.sleep(1);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		player.CalcCount();
		//player.lock.unlock();
	}
}
