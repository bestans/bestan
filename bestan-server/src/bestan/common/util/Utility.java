package bestan.common.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.jar.JarFile;

import org.slf4j.Logger;


/**
 * Some generic utility methods.
 */
public class Utility {

	/** no instance allowed */
	private Utility() {
		// static class
	}

	/**
	 * adds some leading '0' to the sting until the length <i>maxDigits</i> is
	 * reached
	 * 
	 * @param number
	 *            the number to convert
	 * @param maxDigits
	 *            the amount of digits expected
	 * @return the expected number
	 */
	public static String addLeadingZeros(String number, int maxDigits) {
		StringBuilder result = new StringBuilder(number);

		while (result.length() < maxDigits) {
			result.insert(0, "0");
		}

		return result.toString();
	}


	/**
	 * creates a nice hex-dump of the byte array
	 * 
	 * @param byteArray
	 *            the byte array to convert.
	 * @return a hex-dump of the array.
	 */
	public static String dumpByteArray(byte[] byteArray) {
		if (byteArray == null) {
			return "null";
		}
		return dumpInputStream(new ByteArrayInputStream(byteArray));
	}

	/**
	 * creates a nice hex-dump of the byte array
	 * 
	 * @param byteStream
	 *            the byte array to convert.
	 * @return a hex-dump of the array.
	 */
	public static String dumpInputStream(InputStream byteStream) {
		StringBuilder result = new StringBuilder();
		try {
			int index = 0;
			StringBuilder chars = new StringBuilder();

			int theByte = byteStream.read();
			result.append(addLeadingZeros(Integer.toHexString(index), 8)).append(' ');
			index++;

			while (theByte != -1) {
				result.append(addLeadingZeros(Integer.toHexString(theByte), 2)).append(' ');

				// show chars < 32 and > 127 as '.'
				if ((theByte > 31) && (theByte < 128)) {
					chars.append((char) (theByte));
				} else {
					chars.append('.');
				}

				if ((index > 0) && (index % 16 == 0)) {
					result.append(chars).append('\n').append(
					        addLeadingZeros(Integer.toHexString(index), 8)).append(' ');

					chars = new StringBuilder();
				}
				index++;
				theByte = byteStream.read();
			}
			return result.toString();
		} catch (Exception e) {
			return result.toString() + "\nException: " + e.getMessage();
		}
	}

	/**
	 * copies an array
	 *
	 * @param array array to copy
	 * @return copy of array
	 */
	public static byte[] copy(byte[] array) {
		byte[] temp = new byte[array.length];
		System.arraycopy(array, 0, temp, 0, array.length);
		return temp;
	}

	/**
	 * copies an array
	 *
	 * @param array array to copy
	 * @return copy of array
	 */
	public static float[] copy(float[] array) {
		float[] temp = new float[array.length];
		System.arraycopy(array, 0, temp, 0, array.length);
		return temp;
	}

	/**
	 * copies an array
	 *
	 * @param array array to copy
	 * @return copy of array
	 */
	public static String[] copy(String[] array) {
		String[] temp = new String[array.length];
		System.arraycopy(array, 0, temp, 0, array.length);
		return temp;
	}

	/**
	 * Sleeps a number of milliseconds.
	 * 
	 * @param ms number of milliseconds to sleep
	 */
	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	/**
	 * byte array to int with BigEndian
	 * @param b
	 * @param pos
	 * @return
	 */
	public static int byteArrayToInt(byte[] b, int pos) {  
	    return   b[pos+3] & 0xFF |  
	            (b[pos+2] & 0xFF) << 8 |  
	            (b[pos+1] & 0xFF) << 16 |  
	            (b[pos] & 0xFF) << 24;  
	}  

	/**
	 * int to byte array with BigEndian
	 * @param a
	 * @return
	 */
	public byte[] intToByteArray(int a) {  
	    return new byte[] {  
	        (byte) ((a >> 24) & 0xFF),  
	        (byte) ((a >> 16) & 0xFF),     
	        (byte) ((a >> 8) & 0xFF),     
	        (byte) (a & 0xFF)  
	    };  
	}
	
	public static byte getByteByIndex(int value, int index) {
		return (byte) ((value >> (index * 8)) & 0xFF);  
	}
	
	public static int setByteByIndex(int b, int value, int index) {
		return (b << (index * 8)) | (~(0xFF << (index * 8)) & value);
	}
	
	public static boolean isMark(final int v, final int index) {
		return ((1 << index) & v) > 0;
	}
	
	public static int markByIndex(final int v, final int index) {
		return v | (1 << index);
	}
	
	public static boolean isMark(final long v, final int index){
		return ((1 << (long)index) & v) > 0;
	}
	
	public static long markByIndex(final long v, final int index){
		return v | (1 << (long)index);
	}
	

	
//	/**
//	 * get 一个数里有多少个1
//	 * @param val
//	 * @return
//	 */
//	public static int getBitNumOfOne(int val) {
//		int n = 0;  
//	    while(0 < val)  
//	    {//这句代码是把nValue 的某位（其实具体点说为从低位算起的第一个值为1  
//	            //的位）及其以后的所有位都变成0  
//	    	val &=(val - 1);  
//	        n++;  
//	    }  
//	    return n; 
//	}
	
	/**
	 * get 一个数里有多少个1
	 * @param val
	 * @return
	 */
	public static int getBitNumOfOne(int val) {
		val = ((0xaaaaaaaa & val)>>1)  + (0x55555555 & val);
		val = ((0xcccccccc & val)>>2)  + (0x33333333 & val);
		val = ((0xf0f0f0f0 & val)>>4)  + (0x0f0f0f0f & val);
		val = ((0xff00ff00 & val)>>8)  + (0x00ff00ff & val);
		val = ((0xffff0000 & val)>>16) + (0x0000ffff & val);
	    return val;
	}
	
	/**
	 * add pref string array
	 * 
	 * @param prefKey
	 * @param array
	 * @return
	 */
	public static String[] addPrefKeyToArray(final String prefKey, final String[] array) {
		if(null == array) {
			return null;
		}
		

		String[] params = new String[array.length];
		
		for(int i = 0; i < array.length; i++) {
			String str = array[i];
			if(null == str) {
				continue;
			}
			
			params[i] = prefKey + str;
		}
		
		return params;
	}
	
	/**
	 * 获取当前线程的堆栈信息
	 * 
	 * @return
	 */
	public static String getCurrentThreadStacktrack() {
		return Thread.currentThread().getName() + ": " + getStacktace(Thread.currentThread().getStackTrace());
	}
	
	/**
	 * 获取thread线程的堆栈
	 * 
	 * @param thread
	 * @return
	 */
	public static String getStacktrackByThread(Thread thread) {
		if(null == thread) {
			return "thread is null";
		}
		
		return thread.getName() + ": " + getStacktace(thread.getStackTrace());
	}
	
	/**
	 * 打印所有线程堆栈
	 * @param logger
	 */
	public static void dumpAllStacktrace(Logger logger) {
		if(null == logger) {
			return;
		}
		
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
			sb.append(entry.getKey().getName());
			sb.append(getStacktace(entry.getValue()));
		}
		
		logger.error(sb.toString());
	}
	
	private static String getStacktace(StackTraceElement[] stackTraceElements) {
		if(null == stackTraceElements) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for(StackTraceElement element : stackTraceElements) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static int[] string2IntArray(String str, String filter) {
		String[] parseStrList = str.split(filter);
		if (parseStrList == null) {
			return null;
		}
        int[] tempArr = new int[parseStrList.length];
        for (int i = 0; i < parseStrList.length; ++i) {
			if (parseStrList[i].isEmpty()) {
				return null;
			}
        	tempArr[i] = Integer.valueOf(parseStrList[i]).intValue();
        }
        
        return tempArr;
	}
	
	public static short[] string2ShortArray(String str, String filter) {
		String[] parseStrList = str.split(filter);
		if (parseStrList == null) {
			return null;
		}
		short[] tempArr = new short[parseStrList.length];
        for (int i = 0; i < parseStrList.length; ++i) {
			if (parseStrList[i].isEmpty()) {
				return null;
			}
        	tempArr[i] = Short.valueOf(parseStrList[i]).shortValue();
        }
        
        return tempArr;
	}
	
	public static byte[] string2ByteArray(String str, String filter) {
		String[] parseStrList = str.split(filter);
		if (parseStrList == null) {
			return null;
		}
        byte[] tempArr = new byte[parseStrList.length];
        for (int i = 0; i < parseStrList.length; ++i) {
			if (parseStrList[i].isEmpty()) {
				return null;
			}
        	tempArr[i] = Byte.parseByte(parseStrList[i]); 
        }
        
        return tempArr;
	}
	
	/**
	 * get JarFile
	 * 
	 * @param aclass
	 * @return
	 * @throws Exception
	 */
	public static JarFile getJarFile(Class<?> aclass, final String filePathName, final String jarName) throws Exception {
		String className = aclass.getSimpleName() + ".class";

		String path = aclass.getResource(className).getPath();
		String jarFilePath = path;
		if (jarFilePath.contains("file:")) {
			jarFilePath = jarFilePath.substring(jarFilePath.indexOf(":") + 1, jarFilePath.indexOf("!"));
		} else if (jarFilePath.contains("classes")) {
			jarFilePath = jarFilePath.substring(0, jarFilePath.indexOf("classes"));
			jarFilePath = jarFilePath + File.separator + filePathName + File.separator + jarName;
		} else {
			throw new Exception("getJarPath fail, jarFilePath: " + jarFilePath);
		}
		jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
		jarFilePath = jarFilePath.replace(" ","%20");
		
		return new JarFile(jarFilePath);
	}
	
	public static InetAddress getIPByHost(String url) throws UnknownHostException {  
		return InetAddress.getByName(url);
	} 
}
