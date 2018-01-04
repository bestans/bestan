package com.bestan.mvntest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import bestan.pb.NetCommon.*;

import com.alibaba.fastjson.JSON;
import com.bestan.mvntest.Group;
import com.bestan.mvntest.User;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	test3();
    }
    
    public static void test3()
    {
    	try {
    		test_data td_b = test_data.parseFrom(new FileInputStream("abc.test"));
    	
    		System.out.println(td_b.getValue());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void test2()
    {
    	test_data.Builder td = test_data.newBuilder();
    	td.setValue(10);
    	
    	System.out.println(td.getValue());
    	
    	test_data td_b = td.build();
    	try {
    		FileOutputStream file = new FileOutputStream("abc.test");
			td_b.writeTo(file);
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void test1()
    {
        System.out.println( "Hello World!" );
        
        Group group = new Group();
        group.setId(0L);
        group.setName("admin");

        User guestUser = new User();
        guestUser.setId(2L);
        guestUser.setName("guest");

        User rootUser = new User();
        rootUser.setId(3L);
        rootUser.setName("root");

        group.addUser(guestUser);
        group.addUser(rootUser);

        String jsonString = JSON.toJSONString(group);

        System.out.println(jsonString);
    }
}
