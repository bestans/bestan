package bestan.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import bestan.common.protobuf.NetBase.BaseMessage;
import bestan.pb.NetCommon.*;

public class TestProto {
	public static void main(String[] args) throws Exception{
		try {
			//sendProtobuf();
			sendRequest();
			System.out.println("finish");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static byte[] GetBytes() {
		test_data.Builder builder = test_data.newBuilder();
		builder.setValue(54321);
		test_data td = builder.build();
		
		BaseMessage.Builder bmsg = BaseMessage.newBuilder();
		bmsg.setType(1);
		bmsg.setData(td.toByteString());
		byte[] data = bmsg.build().toByteArray();
		return data;
	}
	
	private static void sendRequest() throws Exception {
	    HttpClient client = HttpClients.createDefault();

	    // 创建GET请求（在构造器中传入URL字符串即可）
	    HttpPost post = new HttpPost("http://127.0.0.1:8080/");
	
		ByteArrayEntity entry = new ByteArrayEntity(GetBytes());
		post.setEntity(entry);
	    // 调用HttpClient对象的execute方法获得响应
	    HttpResponse response = client.execute(post);

	    // 调用HttpResponse对象的getEntity方法得到响应实体
	    HttpEntity httpEntity = response.getEntity();
	    test_data.Builder builder = test_data.newBuilder().mergeFrom(EntityUtils.toByteArray(httpEntity));
	    
	    // 使用EntityUtils工具类得到响应的字符串表示
	    System.out.println(builder.getValue());
	}
	
	private static void sendProtobuf() throws Exception{
		String path = "http://127.0.0.1:8080/";
		
		test_data.Builder builder = test_data.newBuilder();
		builder.setValue(54321);
		test_data td = builder.build();
		
		BaseMessage.Builder bmsg = BaseMessage.newBuilder();
		bmsg.setType(1);
		bmsg.setData(td.toByteString());
		byte[] data = bmsg.build().toByteArray();
		
		java.net.URL url = new java.net.URL(path);
		java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        OutputStream outStream = conn.getOutputStream();
        outStream.write(data);
        outStream.flush();
//        Thread.sleep(10000);
//        System.out.println("end");
//        System.exit(-1);
        outStream.close();
        if(conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) conn.getInputStream(), "UTF-8"));
            
            String msg = in.readLine();
        	System.out.println("msg: " + msg);
            in.close();
        }
        conn.disconnect();
	}
}
