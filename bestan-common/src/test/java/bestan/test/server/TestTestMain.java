package bestan.test.server;

import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.protobuf.MessageFixedEnum;

/**
 * @author yeyouhuan
 *
 */
public class TestTestMain {
	public enum TestEnum {
	}
	public static void test1() throws FormatException {
		throw new FormatException("aaaa%s,%s", "aa");
	}
	
	public static void test2() {
		String aaa = "aaa";
		String bbb = "aaa";
		Glog.debug("aaa={}", aaa.equals(bbb));
		Glog.debug("aa = {}", MessageFixedEnum.BASE_RPC.getMessageClass(), MessageFixedEnum.BASE_RPC.ordinal());
	}
	
	public static void main(String[] args) {
		try {
			test2();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
