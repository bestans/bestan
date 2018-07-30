package bestan.log;

public class Test {
	public static void test2()
	{
		Glog.log.trace("is customize trace log {} data {}", 9, "adfasfd");
		Glog.log.debug("is customize debug log {} data", 10);
		Glog.log.info("is customize info log {} data", 11);
		Glog.log.warn("is customize warn log {} data", 12);
		Glog.log.error("is customize error log {} data", 13);
	}
	public static void main(String[] args) {
		test2();
	}
}
