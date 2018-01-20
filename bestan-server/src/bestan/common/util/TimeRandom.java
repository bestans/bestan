package bestan.common.util;

import java.util.Random;

/**
 * 时间种子工具类
 * @author lixiwen
 *
 */
public class TimeRandom {
	private static Random random;
	
	public static Random getRandom() {
		if(null == random) {
			random = new Random(System.currentTimeMillis());
		}
		
		return random;
	}
}
