package bestan.log;

public class Test {
	public static void test2()
	{
		GLog.log.trace("is customize trace log {} data {}", 9, "adfasfd");
		GLog.log.debug("is customize debug log {} data", 10);
		GLog.log.info("is customize info log {} data", 11);
		GLog.log.warn("is customize warn log {} data", 12);
		GLog.log.error("is customize error log {} data", 13);
	}
	public static void main(String[] args) {
		test2();
	}
}
