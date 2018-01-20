package bestan.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class GZipUtil {
	public static final int BUFFER = 1024;  
	  
    /** 
     * 数据压缩 
     *  
     * @param data 
     * @return 
     * @throws Exception 
     */  
    public static byte[] compress(byte[] data) throws Exception {  
        ByteArrayInputStream bais = new ByteArrayInputStream(data);  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
  
        // 压缩  
        compress(bais, baos);  
  
        byte[] output = baos.toByteArray();  
  
        baos.flush();  
        baos.close();  
  
        bais.close();  
  
        return output;  
    }  
    
    /** 
     * 数据压缩 
     *  
     * @param is 
     * @param os 
     * @throws Exception 
     */  
    public static void compress(InputStream is, OutputStream os)  
            throws Exception {  
  
        GZIPOutputStream gos = new GZIPOutputStream(os);  
  
        int count;  
        byte data[] = new byte[BUFFER];  
        while ((count = is.read(data, 0, BUFFER)) != -1) {  
            gos.write(data, 0, count);  
        }  
  
        gos.finish();  
  
        gos.flush();  
        gos.close();  
    }  
    
    /** 
     * 数据解压缩 
     *  
     * @param data 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decompress(byte[] data) throws Exception {  
        ByteArrayInputStream bais = new ByteArrayInputStream(data);  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
  
        // 解压缩  
  
        decompress(bais, baos);  
  
        byte[] retData = baos.toByteArray();  
  
        baos.flush();  
        baos.close();  
  
        bais.close();  
  
        return retData;  
    }  
    
    /** 
     * 数据解压缩 
     *  
     * @param is 
     * @param os 
     * @throws Exception 
     */  
    public static void decompress(InputStream is, OutputStream os)  
            throws Exception {  
  
        GZIPInputStream gis = new GZIPInputStream(is);  
  
        int count;  
        byte data[] = new byte[BUFFER];  
        while ((count = gis.read(data, 0, BUFFER)) != -1) {  
            os.write(data, 0, count);  
        }  
  
        gis.close();  
    }
}
