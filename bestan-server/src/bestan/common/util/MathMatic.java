package bestan.common.util;

import java.util.Random;

/**
 * Math Class Util
 * @author lixiwen
 *
 */
public class MathMatic {
	public static final int RANDOM_PARENT = 10000;
	
	/**
	 * get [start, end] num by Random
	 * 
	 * @param start 
	 * @param end
	 * @param random obj
	 * 
	 * @return [start, end] num
	 */
	public static int getRandomBetween(final int start, final int end, Random random) {
		return end > start ? random.nextInt(end - start + 1) + start : random.nextInt(start - end + 1) + end;
	}
	
	/**
	 * get [start, end] num by TimerRandom
	 * 
	 * @param start
	 * @param end
	 * 
	 * @return[start, end] num
	 */
	public static int getRandomBetween(final int start, final int end) {
		return end > start ? TimeRandom.getRandom().nextInt(end - start + 1) + start : TimeRandom.getRandom().nextInt(start - end + 1) + end;
	}
	
	/**
	 * get a hunserd percent
	 * 
	 * @return percent
	 */
	public static int getRateHPercent() {
		return getRandom(99) + 1;
	}
	
	/**
	 * get a thousand percent
	 * 
	 * @return percent
	 */
	public static int getRateTPercent() {
		return getRandom(999) + 1;
	}
	
	/**
	 * get a thousand percent
	 * 
	 * @return percent
	 */
	public static int getRateMPercent() {
		return getRandom(9999) + 1;
	}
	
	/**
	 * 获取[start end]n个不重复的随机数
	 * @param start
	 * @param end
	 * @param n
	 * @return
	 */
	public static Integer[] getNoRepeatRandom(int start, int end, int n){
		if (n <= 0 || start > end) {
			return null;
		}
		int len = end - start + 1;
		if (n > len) {
			return null;
		}
		Integer[] source = new Integer[len];
		for (int i = start; i < start + len; i++) {
			source[i - start] = i;
		}
		Integer[] result = new Integer[n];
		Random rd = new Random();
		int index = 0;
		for (int i = 0; i < result.length; i++) {
			index = Math.abs(rd.nextInt() % len--);
			result[i] = source[index];
			source[index] = source[len];
		}
		return result;
	}
	
	// return [0, maxValue]
	public static int getRandom(int maxValue){	
		return TimeRandom.getRandom().nextInt(Integer.MAX_VALUE) % (MathMatic.abs(maxValue) + 1);
	}
	
	//return [minValue, MaxValue]
	public static int getRandom(int minValue, int MaxValue){		
		return getRandom(MathMatic.abs(MaxValue - minValue)) + (minValue < MaxValue ? minValue : MaxValue);
	}
	
	/**
	 * abs 
	 * 
	 * @param value
	 * @return
	 */
	public static int abs(int value) {
		return Math.abs(value);
	}
	
	/**
	 * ceil 
	 * 
	 * @param value
	 * @return
	 */
	public static int ceil(double value) {
		return (int)Math.ceil(value);
	}
	
	/**
	 * ceil 
	 * 
	 * @param value
	 * @return
	 */
	public static int ceilUp(double value) {
		int ceilVal = (int)Math.ceil(value);
		return (ceilVal == 0) ? 0 : ceilVal + 1;
	}
	
	/**
	 * getIntSomeBit
	 * @param src
	 * @param mask
	 * @return
	 */
	public static int getIntSomeBit(int src, int mask){
		return src >> mask & 1;
	}
	
	/**
	 * isIntSomeBitTrue
	 * @param src
	 * @param mask
	 * @return
	 */
	public static boolean isIntSomeBitTrue(int src, int mask){
		return getIntSomeBit(src, mask) == 1;
	}
	
    /**
     * SetIntSomeBit
     * @param src
     * @param mask
     * @param flag
     * @return
     */
    public static int setIntSomeBit(int src, int mask, boolean flag) {
        return flag ? (src | (0x1 << mask)) : (src & ~(0x1 << mask));
    }
    
    /**
     * pow 
     * 
     * @param base
     * @param level
     * @return
     */
    public static long pow(int base, int level) {
    	return (long)Math.pow(base, level);
    }
}
