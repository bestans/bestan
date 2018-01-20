package bestan.common.db.command;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;

import bestan.common.db.DBTransaction;
import bestan.common.db.TransactionPool;
import bestan.common.timer.DateValidate;
import bestan.log.GLog;

/**
 * processes DBCommands in the background
 */
class DBCommandQueueBackgroundThread implements Runnable {
	private static Logger logger = GLog.log;
	private static final int RETRY_COUNT = 5;
	private int index;

	public DBCommandQueueBackgroundThread(int index) {
		this.index = index;
	}
	/**
	 * the background thread
	 */
	public void run() {
		logger.debug("DBCommandQueueBackgroundThread[" + index + "] start..");
		DBCommandQueue queue = DBCommandQueue.get();
		while (true) {
			DBCommandMetaData metaData = null;
			try {
				metaData = queue.getNextCommand(index);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}

			if (null != metaData && null != metaData.getCommand()) {
				try {
					processCommand(metaData);
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

	/**
	 * processes a command
	 *
	 * @param metaData meta data about the command to process
	 */
	private void processCommand(DBCommandMetaData metaData) {
		//MDC.put("context", metaData + " ");
		if (TransactionPool.get() == null) {
			logger.error("Database not initialized, skipping database operation");
			return;
		}

		for (int i = 0; i < RETRY_COUNT; i++) {
			if (executeDBAction(metaData)) {
				break;
			}
			logger.warn("Retrying DBCommand " + metaData);
		}

		if (metaData.getCommand() instanceof DBCommandWithCallback) {
			DBCommandWithCallback commandWithCallback = (DBCommandWithCallback) metaData.getCommand();
			commandWithCallback.invokeCallback();
		}

		if (metaData.isResultAwaited()) {
			metaData.setProcessedTimestamp(DateValidate.getCurrentTimeMillis());
			DBCommandQueue.get().addResult(metaData, index);
		}
		//MDC.put("context", "");
	}

	/**
	 * executes the command
	 *
	 * @param metaData DBCommandMetaData
	 * @return true, if the command was executed (sucessfully or unsuccessfully); false if it should be tried again
	 */
	private boolean executeDBAction(DBCommandMetaData metaData) {
		DBTransaction transaction = TransactionPool.get().getFreeDbTransaction();
		try {
			metaData.getCommand().execute(transaction);
			TransactionPool.get().commit(transaction);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			TransactionPool.get().rollback(transaction);
			metaData.getCommand().setException(e);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			if (e.toString().contains("CommunicationsException") || e.toString().contains("Query execution was interrupted")) {
				TransactionPool.get().killTransaction(transaction);
				TransactionPool.get().refreshAvailableTransaction();
				return false;
			}
			TransactionPool.get().rollback(transaction);
			metaData.getCommand().setException(e);
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			TransactionPool.get().rollback(transaction);
			metaData.getCommand().setException(e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			TransactionPool.get().rollback(transaction);
			metaData.getCommand().setException(e);
		}
		return true;
	}
}
