package com.bestan.mvntest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import bestan.pb.NetCommon.*;

import com.alibaba.fastjson.JSON;
import com.bestan.mvntest.Group;
import com.bestan.mvntest.User;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	test5();
    }
    
    public static void test5()
    {
    	System.out.println(System.getProperty("os.arch"));
    	System.out.println(System.getProperty("sun.arch.data.model")); 
    	System.out.println(Convert.bytesToInt(null));
    }
    public static void test4()
    {
    	  // a static method that loads the RocksDB C++ library.
    	  RocksDB.loadLibrary();

    	  try (final Options options = new Options().setCreateIfMissing(true)) {
    	    
    	    // a factory method that returns a RocksDB instance
    	    try (final RocksDB db = RocksDB.open(options, "db")) {
    	    	
    	        ByteArrayOutputStream baos = new ByteArrayOutputStream();    
    	        DataOutputStream dos = new DataOutputStream(baos);
    	        try {
					dos.writeInt(123456);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    	        
    	    	Integer key_int1 = 105;
    	    	byte[] key1 = key_int1.toString().getBytes();
    	    	byte[] src_value = baos.toByteArray();
    	    	System.out.println(src_value);
    	    	// some initialization for key1 and key2
    	    	try {
    	    	  final byte[] value = db.get(key1);
    	    	  if (value != null) {  // value == null if key1 does not exist in db.
    	    		  	System.out.println("find");
    	    		  	
    	    		    ByteArrayInputStream bais = new ByteArrayInputStream(value);    
    	    		    DataInputStream dis = new DataInputStream(bais);
    	    		    try {

        	    	    	System.out.println(dis.readInt());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    	    	  }
    	    	  else
    	    	  {
    	    		  	System.out.println("not find");
      	    	    	db.put(key1, src_value);
    	    	  }
    	    	} catch (RocksDBException e) {
    	    	  // error handling
    	    	}
    	        // do something
    	    }
    	  } catch (RocksDBException e) {
    	    // do some error handling
    	  }

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
