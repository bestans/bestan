package com.bestan.mvntest;

public class TestThread {
	public static void main(String[] args) {
		DBManager.GetInstance().init();
		System.out.println("begin");
		// TODO Auto-generated method stub
		for (int i = 0; i <  10; ++i)
		{
			MyThread t = new MyThread(i);
			t.start();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("end");
	}

}
