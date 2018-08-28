package bestan.test.server;

import java.util.concurrent.Executors;

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
	
	public static void test3() {
		var t = Executors.newFixedThreadPool(1);
		t.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (true)
					{
						System.out.println("aaaa");
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.shutdown();

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.shutdownNow();
	}
	
	public static void main(String[] args) {
		try {
			test3();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
