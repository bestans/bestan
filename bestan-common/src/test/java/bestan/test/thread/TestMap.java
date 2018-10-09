package bestan.test.thread;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author yeyouhuan
 *
 */
public class TestMap {
	public static Map<Long, Long> testm = Maps.newHashMap();
	public static void remove(long key) {
		testm.remove(key);
	}
	
	public static void add(long key, long value) {
		testm.put(key, value);
	}
}
