package bestan.common.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import bestan.common.config.ServerConfig;
import bestan.common.datastruct.Pair;

public class SeqIdUtil {
	private static AtomicLong guestSeqID = new AtomicLong();
	
	private static AtomicLong movieSeqID = new AtomicLong();
	
	private static AtomicInteger playerSeqID = new AtomicInteger();
	
	private static int randNameFStartSeqID;
	private static int randNameFCursor;
	private static int randNameLSeqID;
	private static final int randNameSeed = ServerConfig.getInstance().zoneid % 1000 + 1;
	
	public static final long getGuestSeqID() {
		return guestSeqID.getAndIncrement();
	}
	
	public static final long getMovieSeqID() {
		return movieSeqID.getAndIncrement();
	}
	
	public static final int getPlayerSeqID() {
		return playerSeqID.getAndIncrement();
	}
	
	/**
	 * 分配 randomname pair key
	 * @param rowCount
	 * @return
	 */
	public static final synchronized Pair<Long, Integer> getRandonNameKey(final int rowCount) {
		// 0. 判断FCursor 是否已经到尽头了
		int startSeqId = randNameFStartSeqID % rowCount;
		startSeqId = (startSeqId > 0) ? startSeqId : rowCount;
		
		// 1. 通过start seq id 算出游标的索引最大值
		int endStartCursorIdx = startSeqId + rowCount - 1;
		
		// 2. 判断是否已经是最大值了
		if(randNameFCursor >= endStartCursorIdx) {
			// start seq id ++
			randNameFStartSeqID++;
			startSeqId = randNameFStartSeqID % rowCount;
			startSeqId = (startSeqId > 0) ? startSeqId : rowCount;
			// 设置初始游标
			randNameFCursor = startSeqId;
			// 重置 last index
			randNameLSeqID = randNameSeed;
		} else {
			randNameLSeqID++;
			randNameFCursor++;
		}
		
		// 避免横向一一对应的情况，因为这个名词已被机器人使用
		if(randNameFCursor == randNameLSeqID) {
			randNameFStartSeqID++;
			startSeqId = randNameFStartSeqID % rowCount;
			startSeqId = (startSeqId > 0) ? startSeqId : rowCount;
			// 设置初始游标
			randNameFCursor = startSeqId;
			// 重置 last index
			randNameLSeqID = randNameSeed;
		}

		long retF = (long)randNameFStartSeqID;
		retF = (retF << 32) + randNameFCursor;
		int retL = randNameLSeqID;
		return new Pair<Long, Integer>(retF, retL);
	}

	/** 
	 * 只有一个线程会 执行 Load
	 * @param lSeqId
	 */
	public static void loadGuestSeqID(long lSeqId) {
		guestSeqID.set(lSeqId);
	}
	
	/** 
	 * 只有一个线程会 执行 Load
	 * @param lSeqId
	 */
	public static void loadMovieSeqID(long lSeqId) {
		movieSeqID.set(lSeqId);
	}
	
	public static void loadPlayerSeqID(int nSeqId) {
		playerSeqID.set(nSeqId);
	}
	
	/** 
	 * 只有一个线程会 执行 Load
	 * @param lSeqId
	 */
	public static void loadRandNameSeqID(long lSeqId1, int lSeqId2) {
		randNameFStartSeqID = (int)((lSeqId1 >> 32) & 0xFFFFFFFF);
		randNameFCursor = (int)(lSeqId1 & 0xFFFFFFFF);
		if(0 == lSeqId2) {
			randNameLSeqID = randNameSeed - 1;
		}
		else {
			randNameLSeqID = lSeqId2;
		}
		
	}
}
