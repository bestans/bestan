package bestan.test.thread;

/**
 * @author yeyouhuan
 *
 */
public interface ThreadEventFactory {
	default Runnable createRunable() {
		var r = new Runnable() {
			@Override
			public void run() {
				while (true)
				{	
					commonRun();
				}
			}
		};
		return r;
	}
	void commonRun();
}
