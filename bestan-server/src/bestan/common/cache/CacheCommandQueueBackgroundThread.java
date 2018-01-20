package bestan.common.cache;

import org.slf4j.Logger;
import bestan.log.GLog;
import bestan.common.db.TransactionPool;
import bestan.common.redis.JedisUtil;
import bestan.common.redis.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;


public class CacheCommandQueueBackgroundThread implements Runnable {
	private static final Logger logger = GLog.log;
	private int index;
	
	public CacheCommandQueueBackgroundThread(int index) {
		this.index = index;
	}
	/**
	 * the background thread
	 */
	public void run() {
		logger.debug("CacheCommandQueueBackgroundThread[" + index + "] start..");
		CacheCommandQueue queue = CacheCommandQueue.get();
		while (true) {
			CacheCommand cmd = null;
			try {
				cmd = queue.getNextCommand(index);
			} catch (InterruptedException e) {
				if(!queue.isFinished()) {
					logger.error(e.getMessage(), e);
				} else {
					// 当使用安全退出时会抛出这个异常
					break;
				}
			}

			if (null != cmd) {
				try {
					processCommand(cmd);
				} catch (RuntimeException e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				// There are no more pending commands, check if the server is being shutdown.
				if (queue.isFinished()) {
					break;
				}
			}
		}
		
		logger.warn("=============4. Close " + Thread.currentThread().getName() + " Success.");
	}

	private void processCommand(CacheCommand cmd) {
		if (TransactionPool.get() == null) {
			logger.error("Database not initialized, skipping database operation");
			return;
		}

		execute(cmd);

		if (cmd instanceof CacheCommandWithCallBack) {
			CacheCommandWithCallBack commandWithCallback = (CacheCommandWithCallBack) cmd;
			if(null != commandWithCallback) {
				commandWithCallback.invokeCallback();
			}
		}
	}
	

	private void execute(CacheCommand cmd) {
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getInstance().getJedis();
			cmd.execute(jedis);
		} catch (Exception e) {
			if(null != jedis && (e instanceof JedisConnectionException || e instanceof JedisException)) {
				logger.warn("Get Jedis Exception,", e);
				try {
					RedisUtil.returnBrokenResource(jedis);
					jedis = null;
				} catch(Exception e1) {
					logger.debug("jedis returnBrokenResource Exception**********", e1);
				}
			}
			cmd.setException(e);
			e.printStackTrace();
		} finally {
			try {
				if(null != jedis) {
					RedisUtil.returnResource(jedis);
				}
			} catch(Exception e2) {
				logger.debug("jedis  returnResource Exception**********", e2);
			}
		}
	}
}
