package com.bestan.mvntest;

import java.nio.ByteBuffer;

public class Convert {
	public static byte[] intToBytes(int value)
	{
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(value);
		return buf.array();
	}
	
	public static int bytesToInt(byte[] bytes)
	{
		if (bytes == null) return 0;
		
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.put(bytes);
		buf.flip();
		return buf.getInt();
	}
}
