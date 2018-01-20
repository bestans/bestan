package bestan.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	private static final String CIPHER_PADDING = "AES/ECB/PKCS5Padding";
	private static final String KEY_AES = "AES";
	

	/**
	 * AES 使用16位秘钥 和 Padding模式 解密
	 * @param encryptBytes
	 * @param decryptKey
	 * @return
	 */
    public static String dcryptBy16AndPadding(byte[] encryptBytes, String decryptKey) {  
        if(null == decryptKey || null == encryptBytes) {
        	return null;
        }
        
        try {
	        SecretKeySpec skeySpec = new SecretKeySpec(decryptKey.getBytes("UTF-8"), KEY_AES);
	        Cipher	cipher = Cipher.getInstance(CIPHER_PADDING);
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec);  
	        byte[] decryptBytes = cipher.doFinal(encryptBytes);  
	        return new String(decryptBytes, "UTF-8"); 
        } catch (Exception e) {
			return null;
		}  
    }  
}
