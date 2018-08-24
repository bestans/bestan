package bestan.test.server;

import bestan.common.logic.FormatException;

/**
 * @author yeyouhuan
 *
 */
public class TestTestMain {
	public static void test1() throws FormatException {
		throw new FormatException("aaaa%s,%s", "aa");
	}
	
	public static void main(String[] args) {
		try {
			test1();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
