package bestan.common.server;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import bestan.common.cache.CacheCallBackHandler;
import bestan.common.cache.CacheCommandWithCallBack;
import bestan.common.cache.CacheMessagePack;
import bestan.common.config.ServerConfig;
import bestan.common.datastruct.CircularDoubleBufferQueue;
import bestan.common.db.command.DBCommandWithCallback;
import bestan.common.net.message.AbstractMessageDispatcher;
import bestan.common.net.message.DBMessagePack;
import bestan.common.net.message.DelayedEventHandler;
import bestan.common.net.message.IMessagePack;
import bestan.common.net.message.ProtoMessagePack;
import bestan.log.GLog;


public abstract class AbstractLogicServer extends Thread {
	/** the logger instance. */
	private static final Logger logger = GLog.log;
	
	/**
	 * 逻辑统计类
	 */
	protected class LogicStatistics {
		public int normCount = 0;
		public int dbCount = 0;
		public long lockTime = 0L;
		public long normTime = 0L;
		public long dbTime = 0L;
		public int storeCount = 0;
		public int cacheCount = 0;
		public long cacheTime = 0L;
		
		@Override
		public String toString() {
			StringBuilder os = new StringBuilder("[LogicStatistics] ");
			os.append("lockTime=" + (lockTime / 1000000) + ",");
			os.append("normCount=" + normCount + ",");
			os.append("normTime=" + (normTime / 1000000) + ",");
			os.append("dbCount=" + dbCount + ",");
			os.append("dbTime=" + (dbTime / 1000000) + ",");
			os.append("cacheCount=" + cacheCount + ",");
			os.append("cacheTime=" + (cacheTime / 1000000) + ",");
			os.append("storeCount=" + storeCount);
			
			return os.toString();
		}
		
		public void clean() {
			this.lockTime = 0;
			this.normCount = 0;
			this.normTime = 0L;
			this.dbCount = 0;
			this.dbTime = 0L;
			this.cacheCount = 0;
			this.cacheTime = 0L;
		}
		
		public void initTime(long startNanoTime) {
			this.lockTime = startNanoTime;
			this.normTime = startNanoTime;
			this.dbTime = startNanoTime;
		}
		
		
	}
	
	protected static final int G_QUEUE_DELAY_TIME = 1;
	
	/** 主循环  keepRunning */
	protected volatile boolean keepRunning;

	/** 是否是待关闭状态. */
	protected volatile boolean isDoFinish;
	
	/** msg queue */
	protected CircularDoubleBufferQueue<IMessagePack> msgHandleQueue;
	
	/** total tick count */
	protected long tickIndex;
	
	/** 统计数据 */
	protected LogicStatistics statisticsData = null;
	
	/** 网络消息分发器 */
	protected AbstractMessageDispatcher messageDispatcher;
	
	/** 
	 * 构造函数
	 * 
	 * @param threadName
	 */
	protected AbstractLogicServer(String threadName) {
		super(threadName);
		this.keepRunning = true;
		this.isDoFinish = false;
		this.tickIndex = 0L;
		this.msgHandleQueue = new CircularDoubleBufferQueue<IMessagePack>(1024*1024);
		this.messageDispatcher = null;
	}
	
	
	/**
	 * 反向put msg in queue
	 * 
	 * @param msgPack
	 */
	public void putToMessageQueue(IMessagePack msgPack) {
		if(null == msgPack) {
			return;
		}
		try {
			this.msgHandleQueue.offer(msgPack, G_QUEUE_DELAY_TIME, TimeUnit.MICROSECONDS);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化
	 * 
	 * @param messageDispatcher
	 * @return
	 */
	public boolean init(AbstractMessageDispatcher messageDispatcher) {
		if(null == messageDispatcher) {
			return false;
		}
		
		this.messageDispatcher = messageDispatcher;
		
		if(ServerConfig.getInstance().onLogicThreadStatistics) {
			this.statisticsData = new LogicStatistics();
		}
		return true;
	}
	
	/**
	 * 是否初始化
	 * 
	 * @return
	 */
	protected boolean isInit() {
		return (null != messageDispatcher);
	}
	
	/**
	 * 设置为待关闭状态
	 * MainServer 守护线程调用
	 */
	public void finish() {
		isDoFinish = true;
	}
	
	/**
	 * 自己线程内执行处理关闭流程
	 */
	protected void _excuteFinish() {
		this.keepRunning = false;
	}
	
	/**
	 * Message Processor
	 * 
	 * @throws InterruptedException
	 */
	protected void doMessageHandlerTick() throws InterruptedException {
		if(null != this.statisticsData) {
			this.statisticsData.clean();
		}
		long statisticsTestTime = 0L;
		
		int nReadCount = this.msgHandleQueue.getReadLength();
		int nWriteCount = 0;
		if(nReadCount <= 0) {
			nWriteCount = this.msgHandleQueue.getWriteLength();
			if(nWriteCount <= 0) {
				return;
			}
			
			// 统计上锁时间
			if(null != this.statisticsData) {
				statisticsTestTime = System.nanoTime();
			}
			
			IMessagePack msgPack = this.msgHandleQueue.poll(G_QUEUE_DELAY_TIME, TimeUnit.MICROSECONDS);
			
			if(null != this.statisticsData) {
				this.statisticsData.lockTime = System.nanoTime() - statisticsTestTime;
			}
			
			
			if(logger.isDebugEnabled()) {
				logger.debug("Get MainLogic F Count:1");
			}
			
			if(null == msgPack) {
				logger.error("logicmainthread get msg is nil");
			} else {
				if(null != this.statisticsData) {
					statisticsTestTime = System.nanoTime();
				}
				
				if(msgPack instanceof ProtoMessagePack) {
					ProtoMessagePack msgTmPack = (ProtoMessagePack)msgPack; 
					try {
						messageDispatcher.dispatchMessage(msgTmPack.getChannelContext(), msgTmPack);
					} catch (Exception e) {
						logger.error("Main Thread excute ProtoMessagePack Handler exception:", e);
					}
					
					if(null != this.statisticsData) {
						this.statisticsData.normCount++;
						this.statisticsData.normTime = System.nanoTime() - statisticsTestTime;
					}
				} else if (msgPack instanceof DBMessagePack){
					DBMessagePack msgTmPack = (DBMessagePack)msgPack; 
					DelayedEventHandler delayedEventHandler = msgTmPack.getDelayedEventHandler();
					DBCommandWithCallback dbCommand = msgTmPack.getDbCommand();
					if(null != delayedEventHandler && null != dbCommand) {
						try {
							delayedEventHandler.handleDelayedEvent(dbCommand);
						} catch (Exception e) {
							logger.error("Main Thread excute DBMessagePack Handler exception:", e);
						}
					} else {
						logger.error("dispatch DBMessagePack param is null");
					}
					
					if(null != this.statisticsData) {
						this.statisticsData.dbCount++;
						this.statisticsData.dbTime = System.nanoTime() - statisticsTestTime;
					}
				} else if (msgPack instanceof CacheMessagePack) {
					CacheMessagePack msgCachePack = (CacheMessagePack) msgPack; 
					CacheCallBackHandler cacheCallBackHandler = msgCachePack.getCallBackHandler();
					CacheCommandWithCallBack cacheCommand = msgCachePack.getCommand();
					if(null != cacheCallBackHandler && null != cacheCommand) {
						try {
							cacheCallBackHandler.handleCacheEvent(cacheCommand);
						} catch (Exception e) {
							logger.error("Main Thread excute CacheMessagePack Handler exception:", e);
						}
					} else {
						logger.error("dispatch CacheMessagePack param is null");
					}
					
					if(null != this.statisticsData) {
						this.statisticsData.cacheCount++;
						this.statisticsData.cacheTime = System.nanoTime() - statisticsTestTime;
					}
				}
			}
		}
		
		nReadCount = this.msgHandleQueue.getReadLength();
		
		if(nReadCount <= 0) {
			return;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Get MainLogic L Count:" + nReadCount);
		}
		
		
		int limitProcessNum = ServerConfig.getInstance().onetick_process_limitnum;
		nReadCount = (nReadCount > limitProcessNum) ? limitProcessNum : nReadCount;
		for(int index = 0; index < nReadCount; index++) {
			if(null != this.statisticsData) {
				statisticsTestTime = System.nanoTime();
			}
			IMessagePack msgPack = this.msgHandleQueue.poll(G_QUEUE_DELAY_TIME, TimeUnit.MICROSECONDS);
			if(null != this.statisticsData) {
				this.statisticsData.lockTime += System.nanoTime() - statisticsTestTime;
			}
			
			
			if(null == msgPack) {
				logger.error("logicmainthread get msg is nil");
			} else {
				if(null != this.statisticsData) {
					statisticsTestTime = System.nanoTime();
				}
				if(msgPack instanceof ProtoMessagePack) {
					ProtoMessagePack msgTmPack = (ProtoMessagePack)msgPack;
					try {
						messageDispatcher.dispatchMessage(msgTmPack.getChannelContext(), msgTmPack);
					} catch (Exception e) {
						logger.error("Main Thread excute ProtoMessagePack Handler exception:", e);
					}
				
					if(null != this.statisticsData) {
						this.statisticsData.normCount++;
						this.statisticsData.normTime += System.nanoTime() - statisticsTestTime;
					}
				} else if (msgPack instanceof DBMessagePack){
					DBMessagePack msgTmPack = (DBMessagePack)msgPack; 
					DelayedEventHandler delayedEventHandler = msgTmPack.getDelayedEventHandler();
					DBCommandWithCallback dbCommand = msgTmPack.getDbCommand();
					if(null != delayedEventHandler && null != dbCommand) {
						try {
							delayedEventHandler.handleDelayedEvent(dbCommand);
						} catch (Exception e) {
							logger.error("Main Thread excute DBMessagePack Handler exception:", e);
						}
					} else {
						logger.error("dispatch DBMessagePack param is null");
					}
				
					if(null != this.statisticsData) {
						this.statisticsData.dbCount++;
						this.statisticsData.dbTime += System.nanoTime() - statisticsTestTime; 
					}
				} else if (msgPack instanceof CacheMessagePack) {
					CacheMessagePack msgCachePack = (CacheMessagePack) msgPack; 
					CacheCallBackHandler cacheCallBackHandler = msgCachePack.getCallBackHandler();
					CacheCommandWithCallBack cacheCommand = msgCachePack.getCommand();
					if(null != cacheCallBackHandler && null != cacheCommand) {
						try {
							cacheCallBackHandler.handleCacheEvent(cacheCommand);
						} catch (Exception e) {
							logger.error("Main Thread excute CacheMessagePack Handler exception:", e);
						}
					} else {
						logger.error("dispatch CacheMessagePack param is null");
					}
					
					if(null != this.statisticsData) {
						this.statisticsData.cacheCount++;
						this.statisticsData.cacheTime = System.nanoTime() - statisticsTestTime;
					}
				}
			}// end of if is msgPack null
		}// end of for
	}
	
	protected long getTickIndex() {
		return this.tickIndex;
	}
}
