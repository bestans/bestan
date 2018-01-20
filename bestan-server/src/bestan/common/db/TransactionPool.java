package bestan.common.db;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import bestan.common.config.ServerConfig;
import bestan.common.datastruct.Pair;
import bestan.common.db.adapter.DatabaseAdapter;
import bestan.common.log.LogManager;
import bestan.common.log.Logger;
import bestan.common.util.Utility;

/**
 * Connection Pool
 */
public class TransactionPool {
	/** logger */
	private static Logger logger = LogManager.getLogger(TransactionPool.class);
	
	/** 单例 */
	private static TransactionPool instance = null;
	
	/** 数据库适配器工程句柄 */
	private AdapterFactory factory = null;
	
	/** wait锁 */
	private final Object wait = new Object();
	
	/** 初始连接个数 */
	private int count = 10;
	
	/** 会话总队列 */
	private final List<DBTransaction> dbtransactions = Collections.synchronizedList(new LinkedList<DBTransaction>());
	
	/** 空闲队列 */
	private final List<DBTransaction> freeDBTransactions = Collections.synchronizedList(new LinkedList<DBTransaction>());
	
	/** 线程栈内数据 会话集合 即 {@link DBCommandQueueBackgroundThread} */
	private final ThreadLocal<Set<DBTransaction>> threadTransactions = new ThreadLocal<Set<DBTransaction>>();
	
	/** 正在使用的会话堆栈表  ConcurrentHashMap */
	private final Map<DBTransaction, Pair<String, StackTraceElement[]>> callers;
	
	/** 关闭标识 */
	private boolean closed = false;

	/**
	 * creates a DBTransactionPool
	 *由{@link DatabaseBoostrap} 初始化时调用
	 *
	 * @param connfiguration configuration
	 */
	public TransactionPool() {
		count = ServerConfig.getInstance().db_pool_size;
		callers = new ConcurrentHashMap<DBTransaction, Pair<String, StackTraceElement[]>>(); 
//		callers = Collections.synchronizedMap(new HashMap<DBTransaction, Pair<String, StackTraceElement[]>>());
		factory = new AdapterFactory();
	}

	/**
	 * registers this TransactionPool as the global one.
	 */
	public void registerGlobally() {
		registerGlobal(this);
	}

	/**
	 * registers a TransactionPool as the global one.
	 *
	 * @param transactionPool the pool to register globally
	 */
	private static void registerGlobal(TransactionPool transactionPool) {
		TransactionPool.instance = transactionPool;
	}

	/**
	 * gets the TransactionPool
	 *
	 * @return TransactionPool
	 */
	public static synchronized TransactionPool get() {
		return instance;
	}

	/**
	 * 创建count个会话即连接 相当于连接池，第一次为空时才会创建
	 */
	private void createDBTransactions() {
		synchronized (wait) {
			while (dbtransactions.size() < count) {
				// 创建连接
				DatabaseAdapter adapter = factory.create();
				if (adapter == null) {
					// 只有在出现异常时才会创建失败， 通常为超时
					Utility.sleep(1000);
					continue;
				}
				// 创建会话
				DBTransaction dbtransaction = new DBTransaction(adapter);
				// 添加到总队列
				dbtransactions.add(dbtransaction);
				// 添加到空闲队列
				freeDBTransactions.add(dbtransaction);
			}
		}
	}

	/**
	 * starts a transaction and marks it as reserved
	 * 获取一个可用的线程
	 *
	 * @return DBTransaction
	 */
	public DBTransaction getFreeDbTransaction() {
		if (closed) {
			throw new RuntimeException("transaction pool has been closed");
		}
		
		DBTransaction dbtransaction = null;
		while (dbtransaction == null) {
			synchronized (wait) {
				createDBTransactions();
				if (freeDBTransactions.size() == 0) {
					try {
						logger.debug("Waiting for a DBTransaction", new Throwable());
						dumpOpenTransactions();
						// 等待 freeDBTransaction
						wait.wait();
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				} else {
					// Pop 第一个空闲的连接即会话
					dbtransaction = freeDBTransactions.remove(0);
					// 加入到调用线程栈内集合
					addThreadTransaction(dbtransaction);
				}
				// TODO: check that the connection is still alive
			}
		}
		//logger.debug("getDBTransaction: " + dbtransaction, new Throwable());

		Thread currentThread = Thread.currentThread();
		// 存储调用信息
		callers.put(dbtransaction, new Pair<String, StackTraceElement[]>(currentThread.getName(), currentThread.getStackTrace()));
		
		// 设置会话使用线程
		dbtransaction.setThread(currentThread);
		return dbtransaction;
	}

	/**
	 * dumps a list of open transactions with their threads and stacktraces to the log file.
	 */
	public void dumpOpenTransactions() {
		for (Pair<String, StackTraceElement[]> pair : callers.values()) {
			logger.warn("      * " + pair.first() + " " + Arrays.asList(pair.second()));
		}
	}

	/**
	 * commits this transaction and frees its reservation
	 *
	 * @param dbtransaction transaction
	 * @throws SQLException in case of an database error
	 */
	public void commit(DBTransaction dbtransaction) throws SQLException {
		try {
			dbtransaction.commit();
		} catch (SQLException e) {
			killTransaction(dbtransaction);
			throw e;
		}
		freeDBTransaction(dbtransaction);
	}

	/**
	 * rolls this transaction back and frees the reservation
	 *
	 * @param dbtransaction transaction
	 */
	public void rollback(DBTransaction dbtransaction) {
		try {
			dbtransaction.rollback();
			freeDBTransaction(dbtransaction);
		} catch (SQLException e) {
			killTransaction(dbtransaction);
			logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * 释放该transaction 放入空闲队列
	 * @param dbtransaction
	 */
	private void freeDBTransaction(DBTransaction dbtransaction) {
		if(logger.isDebugEnabled()) {
			logger.debug("freeDBTransaction: " + dbtransaction);
		}
		
		synchronized (wait) {
			threadTransactions.get().remove(dbtransaction);
			callers.remove(dbtransaction);
			dbtransaction.setThread(null);
			if (dbtransactions.contains(dbtransaction)) {
				freeDBTransactions.add(dbtransaction);
			} else {
				logger.error("Unknown DBTransaction " + dbtransaction + " was not freed.", new Throwable());
			}
			wait.notifyAll();
		}
	}

	/**
	 * 加入到线程栈自己的集合中 每个调用此线程都有一个 set
	 * @param dbtransaction
	 */
	private void addThreadTransaction(DBTransaction dbtransaction) {
		Set<DBTransaction> set = threadTransactions.get();
		if (set == null) {
			set = new HashSet<DBTransaction>();
			threadTransactions.set(set);
		}
		set.add(dbtransaction);
	}

	/**
	 * Kicks all transactions which were started in the current thread
	 */
	public void kickHangingTransactionsOfThisThread() {
		Set<DBTransaction> set = threadTransactions.get();
		if ((set == null) || set.isEmpty()) {
			return;
		}

		synchronized (wait) {
			for (DBTransaction dbtransaction : set) {
				killTransaction(dbtransaction);
				logger.error("Hanging transaction " + dbtransaction + " was kicked.");
			}
			wait.notifyAll();
		}
		set.clear();
	}

	/**
	 * removes transactions from the pool that are not connected to the databaes anymore
	 */
	public void refreshAvailableTransaction() {
		synchronized (wait) {
			for (DBTransaction dbtransaction : new HashSet<DBTransaction>(freeDBTransactions)) {
				try {
					dbtransaction.setThread(Thread.currentThread());
					dbtransaction.querySingleCellInt("SELECT 1");
					dbtransaction.setThread(null);
				} catch (SQLException e) {
					logger.warn("Killing dead transaction " + dbtransaction + ".");
					killTransaction(dbtransaction);
				}
			}
		}
	}

	/**
	 * kills a transaction by rolling it back and closing it;
	 * it will be removed from the pool
	 *
	 * @param dbtransaction DBTransaction
	 */
	public void killTransaction(DBTransaction dbtransaction) {
		try {
			dbtransaction.rollback();
		} catch (SQLException e) {
			if(logger.isDebugEnabled()) {
				logger.debug(e.getMessage(), e);
			}
		}
		
		// 关闭连接
		dbtransaction.close();
		// 从本地移除该会话
		dbtransactions.remove(dbtransaction);
		freeDBTransactions.remove(dbtransaction);
		callers.remove(dbtransaction);
		
		// 移除在这个线程正在执行的
		threadTransactions.get().remove(dbtransaction);
	}

	/**
	 * closes the transaction pool
	 */
	public void close() {
		closed = true;
		for (DBTransaction transaction : dbtransactions) {
			transaction.close();
		}
		
		// 释放所有本地资源
		dbtransactions.clear();
		freeDBTransactions.clear();
		callers.clear();
	}
}
