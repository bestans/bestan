package bestan.db.test;


public class TestStatic {
	public int value;
	
    private static class SingletonClassInstance{
        private static final TestStatic instance=new TestStatic();
    }
     
    private TestStatic(){
    	System.out.println("TestStatic");
    	value= 10;
    }
     
    public static TestStatic getInstance(){
        return SingletonClassInstance.instance;
    }
    
    public static void output() {
    	System.out.println("output");
    }
}
