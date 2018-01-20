package bestan.common.util;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;

import bestan.log.GLog;


/**
 * js编转码java实现类.
 */
public class Escape {
	/** 记录日志的变量  */
	private static final Logger logger = GLog.log;
	/** 不转吗的字符  */
	private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyz" +
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";

	/** The Constant hex. */
	private final static String[] hex = { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", 
		"0A", "0B", "0C", "0D", "0E", "0F", "10", "11", "12", "13", "14", "15", "16",
	                "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F", "20", "21", "22", "23",
	                "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F", "30",
	                "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D",
	                "3E", "3F", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A",
	                "4B", "4C", "4D", "4E", "4F", "50", "51", "52", "53", "54", "55", "56", "57", 
	                "58", "59", "5A", "5B", "5C", "5D", "5E", "5F", "60", "61", "62", "63", "64",
	                "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F", "70", "71",
	                "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E",
	                "7F", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B",
	                "8C", "8D", "8E", "8F", "90", "91", "92", "93", "94", "95", "96", "97", "98",
	                "99", "9A", "9B", "9C", "9D", "9E", "9F", "A0", "A1", "A2", "A3", "A4", "A5",
	                "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF", "B0", "B1", "B2",
	                "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
	                "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC",
	                "CD", "CE", "CF", "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", 
	                "DA", "DB", "DC", "DD", "DE", "DF", "E0", "E1", "E2", "E3", "E4", "E5", "E6",
	                "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF", "F0", "F1", "F2", "F3",
	                "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF" };

	/** The Constant val. */
	private final static byte[] val = { 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
		0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x00,
	                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x0A, 0x0B, 0x0C, 0x0D,
	                0x0E, 0x0F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 
	                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F };

	/**
	 * 编码
	 * @param s 源字符串
	 * @return 编码字符串
	 */
	public static String escape(String s) {
		if (s != null) {
			StringBuffer sbuf = new StringBuffer();
			int len = s.length();
			for (int i = 0; i < len; i++) {
				int ch = s.charAt(i);
				if ('A' <= ch && ch <= 'Z') {
					sbuf.append((char) ch);
				} else if ('a' <= ch && ch <= 'z') {
					sbuf.append((char) ch);
				} else if ('0' <= ch && ch <= '9') {
					sbuf.append((char) ch);
				} else if (ch == '-' || ch == '_' || ch == '.' || ch == '!' || ch == '~' 
						|| ch == '*' || ch == '\'' || ch == '(' || ch == ')') {
					sbuf.append((char) ch);
				} else if (ch <= 0x007F) {
					sbuf.append('%');
					sbuf.append(hex[ch]);
				} else {
					sbuf.append('%');
					sbuf.append('u');
					sbuf.append(hex[(ch >>> 8)]);
					sbuf.append(hex[(0x00FF & ch)]);
				}
			}
			return sbuf.toString();
		}
		return null;
	}


	/**
	 * 解码 说明：本方法保证 不论参数s是否经过escape()编码，均能得到正确的“解码”结果
	 * @param s 源字符串
	 * @return 解码字符串
	 */
	public static String unescape(String s) {
		if (s != null) {
			StringBuffer sbuf = new StringBuffer();
			int i = 0;
			int len = s.length();
			while (i < len) {
				int ch = s.charAt(i);
				if ('A' <= ch && ch <= 'Z') {
					sbuf.append((char) ch);
				} else if ('a' <= ch && ch <= 'z') {
					sbuf.append((char) ch);
				} else if ('0' <= ch && ch <= '9') {
					sbuf.append((char) ch);
				} else if (ch == '-' || ch == '_' || ch == '.' || ch == '!' || ch == '~' || ch == '*' 
						|| ch == '\'' || ch == '(' || ch == ')') {
					sbuf.append((char) ch);
				} else if (ch == '%') {
					int cint = 0;
					if ('u' != s.charAt(i + 1)) {
						cint = (cint << 4) | val[s.charAt(i + 1)];
						cint = (cint << 4) | val[s.charAt(i + 2)];
						i += 2;
					} else {
						cint = (cint << 4) | val[s.charAt(i + 2)];
						cint = (cint << 4) | val[s.charAt(i + 3)];
						cint = (cint << 4) | val[s.charAt(i + 4)];
						cint = (cint << 4) | val[s.charAt(i + 5)];
						i += 5;
					}
					sbuf.append((char) cint);
				} else {
					sbuf.append((char) ch);
				}
				i++;
			}
			return sbuf.toString();
		}
		return null;
	}

	/**
	 * encodeURIComponent，它是将中文、韩文等特殊字符转换成utf-8格式的url编码,
	 * <p>encodeURIComponent不编码字符有71个：!， '，(，)，*，-，.，_，~，0-9，a-z，A-Z</p>
	 * @param input 源字符串
	 * @return 编码后的字符串
	 */
	public static String encodeURIComponent(String input) {
		if (input == null || input.length() == 0) {
			return input;
		}

		int l = input.length();
		StringBuilder o = new StringBuilder(l * 3);
		try {
			for (int i = 0; i < l; i++) {
				String e = input.substring(i, i + 1);
				if (ALLOWED_CHARS.indexOf(e) == -1) {
					byte[] b = e.getBytes("utf-8");
					o.append(getHex(b));
					continue;
				}
				o.append(e);
			}
			return o.toString();
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return input;
	}

	/**
	 * 获取16进制的编码
	 * @param buf 源字节数组
	 * @return 16进制的字符串
	 */
	private static String getHex(byte buf[]) {
		StringBuilder o = new StringBuilder(buf.length * 3);
		for (int i = 0; i < buf.length; i++) {
			@SuppressWarnings("cast")
			int n = (int) buf[i] & 0xff;
			o.append("%");
			if (n < 0x10) {
				o.append("0");
			}
			o.append(Long.toString(n, 16).toUpperCase());
		}
		return o.toString();
	}

	/**
	 * 对应encodeURIComponent进行解码
	 * @param encodedURI 已编码的源字符串
	 * @return 解码字符串
	 */
	public static String decodeURIComponent(String encodedURI) {
		if (encodedURI == null || encodedURI.length() == 0) {
			return encodedURI;
		}
		
		char actualChar;
		StringBuffer buffer = new StringBuffer();
		int bytePattern, sumb = 0;

		for (int i = 0, more = -1; i < encodedURI.length(); i++) {
			actualChar = encodedURI.charAt(i);
			switch (actualChar) {
			case '%': {
				actualChar = encodedURI.charAt(++i);
				int hb = (Character.isDigit(actualChar) ? actualChar - '0' : 10
						+ Character.toLowerCase(actualChar) - 'a') & 0xF;
				actualChar = encodedURI.charAt(++i);
				int lb = (Character.isDigit(actualChar) ? actualChar - '0' : 10 
						+ Character.toLowerCase(actualChar) - 'a') & 0xF;
				bytePattern = (hb << 4) | lb;
				break;
			}
			case '+': {
				bytePattern = ' ';
				break;
			}
			default: {
				bytePattern = actualChar;
			}
			}

			if ((bytePattern & 0xc0) == 0x80) { // 10xxxxxx
				sumb = (sumb << 6) | (bytePattern & 0x3f);
				if (--more == 0)
					buffer.append((char) sumb);
			} else if ((bytePattern & 0x80) == 0x00) { // 0xxxxxxx
				buffer.append((char) bytePattern);
			} else if ((bytePattern & 0xe0) == 0xc0) { // 110xxxxx
				sumb = bytePattern & 0x1f;
				more = 1;
			} else if ((bytePattern & 0xf0) == 0xe0) { // 1110xxxx
				sumb = bytePattern & 0x0f;
				more = 2;
			} else if ((bytePattern & 0xf8) == 0xf0) { // 11110xxx
				sumb = bytePattern & 0x07;
				more = 3;
			} else if ((bytePattern & 0xfc) == 0xf8) { // 111110xx
				sumb = bytePattern & 0x03;
				more = 4;
			} else { // 1111110x
				sumb = bytePattern & 0x01;
				more = 5;
			}
		}
		return buffer.toString();
	}

	/**
	 * Main测试方法
	 * @param args 输入参数
	 */
	public static void main(String[] args) {
		String s1 = escape("45路--甜蜜*&^##！！全角　＃　＠");
		logger.debug("escape s1= " + s1);
		s1 = unescape(s1);
		logger.debug("unescape s1= " + s1);
		
		String s2 = encodeURIComponent("上地东里4区4号楼2单元402#");
		logger.debug("encodeURIComponent s2= " + s2);
		s2 = decodeURIComponent(s2);
		logger.debug("decodeURIComponent s2= " + s2);
	}

}