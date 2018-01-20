package com.bestan.mvntest;

import bestan.log.GLog;

public class MyThread extends Thread {
	private int id;
	private int times = 0;
	
    public MyThread(int p_id) {
    	id = p_id;
    }
    public void run() {
    	System.out.println("begin:" + id);
    	for (;times < 100; ++times)
    	{
    		try {
				sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		int before_value = Convert.bytesToInt(DBManager.GetInstance().get(Convert.intToBytes(times % 10)));
    		DBManager.GetInstance().put(Convert.intToBytes(times % 10), Convert.intToBytes(before_value + 1));
    		int after_value = Convert.bytesToInt(DBManager.GetInstance().get(Convert.intToBytes(times % 10)));
    		GLog.log.debug("thread={}:index={}:before={}:after={}",  id, times%10, before_value, after_value);
    	}
    }
}
