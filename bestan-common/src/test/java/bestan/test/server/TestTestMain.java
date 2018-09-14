package bestan.test.server;

import java.util.concurrent.Executors;

import bestan.common.event.IEvent;
import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.protobuf.MessageFixedEnum;
import bestan.common.thread.BThreadPoolExecutors;
import bestan.common.timer.BTimer;
import bestan.common.timer.BTimer.TimerModule;
import bestan.common.timer.ITimer;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * @author yeyouhuan
 *
 */
public class TestTestMain {
	public enum TestEnum {
		INT,
		LONG;
		
		public String value;
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
	
	public static void test4() {
		TestEnum v1 = TestEnum.INT;
		v1.value = "1111";
		TestEnum v2 = TestEnum.INT;
		v2.value = "22222";
		System.out.println(v1.value);
		System.out.println(v2.value);
	}
	public static void test5() {
//		Reflections reflections = new Reflections("io.netty.buffer");
//		ScanResult scanResult =
//		        new ClassGraph()
//	            .enableAllInfo()
//	            .whitelistPackages("io.netty.buffer")
//	            .scan();
	            
//	    ClassInfoList controlClasses = scanResult.getSubclasses("bestan.common.net.handler.IMessageHandler");
//	    List<Class<?>> controlClassRefs = controlClasses.loadClasses();
		
//		   ClassInfoList filtered = scanResult
//				   		.filter(classInfo -> {
//			            (classInfo.isInterface() || classInfo.isAbstract())
//			            && classInfo.hasAnnotation("com.xyz.Widget")
//			            && classInfo.hasMethod("open")
//			        });

//	    System.out.println(scanResult.getAllClasses().size());
		
		try (ScanResult scanResult =
		        new ClassGraph()
		            .enableAllInfo()
		            .whitelistPackages("bestan.common.net.handler")
		            .scan()) {
//		    ClassInfoList filtered = scanResult.getAllClasses()
//		        .filter(classInfo -> { 
//		        	return !(classInfo.isInterface() || classInfo.isAbstract())
//		        			&& classInfo.implementsInterface("bestan.common.net.handler.IMessageHandler");
//		        });
			ClassInfoList filtered = scanResult.getClassesImplementing("bestan.common.net.handler.IMessageHandler");
		    System.out.println(filtered);
		}
	}
	
	public static void test6() {
		var timer = new TimerModule(BThreadPoolExecutors.newMutipleSingleThreadPool("timer", 1), 100);
		timer.startup();
		Glog.debug("start");
		BTimer.schedule(new IEvent() {
			@Override
			public void run() {
				Glog.debug("aaaa");
			}
		}, 1000);
		
		BTimer.attach(new ITimer() {
			@Override
			public void executeTick() {
				Glog.debug("timer timer");
			}
		}, 1000);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timer.close();
	}
	public static void main(String[] args) {
		try {
			test6();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
