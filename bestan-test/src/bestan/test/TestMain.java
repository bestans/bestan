package bestan.test;

import java.util.Map;

import com.google.common.collect.Maps;

import bestan.common.log.Glog;

public class TestMain {
	public static int getHash(String nameStr) {
		int value = 0;
		var name = nameStr.toLowerCase();
		for (int i = 0; i < name.length(); ++i) {
			value += (name.charAt(i) - Character.valueOf('a')) * (int)Math.pow(26, i);
			Glog.debug("index={}", name.charAt(i) - Character.valueOf('a'));
		}
		return value;
	}
	public static void test3() {
		Glog.debug("v={}", getHash("bbb"));
	}
	public static void test2() {
		Map<Integer, Integer> m = Maps.newHashMap();
		m.put(1, 10);
		Glog.debug("value={}", m.get(10).intValue());
	}
	public static void test1() {
		try {
			var method = Test1.class.getMethod("getValue");
			Glog.debug("value={}", method.invoke(null));
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		test3();
	}
	
	private static class Test1 {
		public static int getValue() {
			return 10;
		}
	}
}
