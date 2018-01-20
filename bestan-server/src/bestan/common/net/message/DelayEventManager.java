package bestan.common.net.message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import bestan.common.datastruct.Pair;
import bestan.common.log.LogManager;

public class DelayEventManager {
	/** the logger instance. */
	private static final bestan.common.log.Logger logger = LogManager.getLogger(DelayEventManager.class);

	private boolean keepRunning = true;
	private BlockingQueue<Pair<DelayedEventHandler, Object>> queue;

	/**
	 * Creates a new DelayEventManager
	 *
	 * @param rpMan RPServerManager
	 */
	public DelayEventManager() {
		queue = new LinkedBlockingQueue<Pair<DelayedEventHandler, Object>>();
	}

	/**
	 * adds a delayed event for processing.
	 *
	 * @param handler DelayedEventHandler
	 * @param data data for the handler
	 */
	public void addDelayedEvent(DelayedEventHandler handler, Object data) {
		try {
			queue.add(new Pair<DelayedEventHandler, Object>(handler, data));
		} catch (NullPointerException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void run() {
		int nSize = queue.size();
		try {
			for(int i = 0; i < nSize; i++) {
				Pair<DelayedEventHandler, Object> entry = queue.poll(1, TimeUnit.MILLISECONDS);
				if (entry == null) {
					if (!keepRunning) {
						return;
					}
				} else {
					entry.first().handleDelayedEvent(entry.second());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * Should the thread be kept running?
	 *
	 * @param keepRunning set to false to stop it.
	 */
	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}

}
