package bestan.test.server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;

import com.google.common.util.concurrent.RateLimiter;

import bestan.common.event.IEvent;
import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.lua.BaseLuaConfig;
import bestan.common.protobuf.MessageFixedEnum;
import bestan.common.thread.BThreadPoolExecutors;
import bestan.common.timer.BTimer;
import bestan.common.timer.BTimer.TimerModule;
import bestan.common.timer.ITimer;
import bestan.test.download.TestServerConfig;
import bestan.test.thread.ReadWrite;
import bestan.test.thread.TestMap;
import bestan.test.thread.ThreadEventFactory;
import bestan.test.thread.ThreadUnit;
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

	public static void main(String[] args) {
		try {
			//ExceptionUtil.sendEmail("632469297@qq.com", "100");
			test14();
			System.out.println("finish");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class NewTestServerConfig extends TestServerConfig {
		
	}
	public static void test14() {
		NewTestServerConfig vConfig = new NewTestServerConfig();
		Integer vInteger = Integer.decode("19");
		Glog.debug("xx={},{}", BaseLuaConfig.class.isAssignableFrom(vConfig.getClass()), vInteger.getClass().getGenericSuperclass());
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

	public static void test7() throws InterruptedException {
    	Long start = System.currentTimeMillis();
        RateLimiter limiter = RateLimiter.create(10.0); // 每秒不超过10个任务被提交
        Thread.sleep(1000);
        for (int i = 0; i < 20; i++) {
        	if (limiter.tryAcquire(11)) {
                Long end = System.currentTimeMillis();
                System.out.println(end - start);
        	} else {
        		--i;
        		Thread.sleep(1000);
        	}
        }
        Long end = System.currentTimeMillis();
        
        System.out.println(end - start);
	}
	
	private static void addFile(File file, String partName) {
		var temp = new File(partName);
		Glog.trace("file:path={},part={},temp={}", file.getAbsolutePath(), partName, temp.length());
	}
	public static void traverseFolder(File file, String partName) {
        if (file == null || !file.exists()) {
        	return;
        }

        partName += "/" + file.getName();
        if (!file.isDirectory()) {
        	addFile(file, partName);
        	return;
        }
        for (var it : file.listFiles()) {
        	traverseFolder(it, partName);
        }
    }
	public static void test8() {
		traverseFolder(new File("testfolder"), ".");
	}
	public static void test9() throws IOException {
		var rf = new RandomAccessFile("testfolder/test1.txt", "r");
		var rf2 = new RandomAccessFile("testfolder/test1.txt", "r");
		System.out.println(rf.readByte());
		System.out.println(rf.readByte());
		System.out.println(rf2.readByte());
	}
	public static void test10() {
		var factory = new ThreadEventFactory() {
			@Override
			public void commonRun() {
				try {
					var rf = new RandomAccessFile("testfolder/test1.txt", "r");
					while (true)
					{
						rf.seek(0);
						Glog.debug("name={},run={}", Thread.currentThread().getName(), rf.readByte());
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		ThreadUnit.createUnit(factory, 10);
	}
	public static void test11() {
		var data = new ReadWrite();
		var factory = new ThreadEventFactory() {
			@Override
			public void commonRun() {
				Glog.debug("id={},read_data={}", Thread.currentThread().getId(), data.read());
			}
		};
		var wfactory = new ThreadEventFactory() {
			@Override
			public void commonRun() {
				data.write();
				Glog.debug("id={},write", Thread.currentThread().getId());
			}
		};
		ThreadUnit.createUnit(factory, 10);
		ThreadUnit.createUnit(wfactory, 5);
	}
	public static void test12() {
		for (int i = 0; i < 10; ++i) {
			TestMap.add(i, i * 10);
		}
		var it = TestMap.testm.entrySet().iterator();
		while (it.hasNext()) {
			var entry = it.next();
			System.out.println("key=" + entry.getKey() + ",value=" + entry.getValue());
			TestMap.remove(entry.getKey());
		}
	}
	public static void test13() throws IOException {
		var src = new File("testsrc");
		var dest = new File("dest1/dest");
		System.out.println(src.exists());
		FileUtils.copyDirectory(src, dest, true);
		FileUtils.forceDelete(dest);
	}
}
