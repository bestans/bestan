package bestan.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringValidateUtil {

	public static List<String> readValidateFile(String path) {
		List<String> list = new LinkedList<String>();
		File file = new File(path);

		if (!file.exists()) {
			return list;
		}

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				// 如果是一行空串，越过
				if (line.trim().equals(""))
					continue;

				list.add(line.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fileReader && null != bufferedReader) {
					fileReader.close();
					bufferedReader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	private static Pattern pattern = Pattern.compile("(http:|https:)//[^[A-Za-z0-9\\._\\?%&+\\-=/#]]*");
	/**
	 * 将一个字符串分段，识别其中的url子串
	 * @param str 原字符串
	 * @param strphases 按照url分割原字符串后的子串列表
	 * @param urlindexes url子串在strphases中的index
	 */
	public static void matchURLsFromString(String str, List<String> strphases, java.util.Set<Integer> urlindexes) {
		Matcher matcher = pattern.matcher(str);
		if(matcher.groupCount() == 0)
			return;
		int whilenum = 0;
		int lastend = 0;
		int urlindex = 0;
		while(matcher.find()) {
			int start = matcher.start();
			if(start > lastend)
			{
				strphases.add(str.substring(lastend, start));
				urlindex++;
			}
			strphases.add(matcher.group());
			urlindexes.add(urlindex);
			lastend = matcher.end();
			urlindex++;
			if(++whilenum > 40) return;//防止while过多
		}
		
		if(lastend < str.length())
			strphases.add(str.substring(lastend, str.length()));
		return;
	}
	
	/**
	 * 将带有url格式的字符串变成带有http标签能被客户端识别的字符串
	 * 例如：
	 * 请访问http://www.163.com或者http://www.sina.com浏览新闻
	 * 转化后为：
	 * <T t="请访问"></T><Http address="http://www.163.com"></Http><T t="或者"></T>
	 * <Http address="http://www.sina.com"></Http><T t="浏览新闻"></T>
	 * @param str 带有url格式的字符串
	 * @return 带有http标签能被客户端识别的字符串
	 */
	public static String convertStringToUrlLabelString(String str) {
		List<String> strphases = new ArrayList<String>();
		java.util.Set<Integer> urlindexes = new HashSet<Integer>();
		matchURLsFromString(str, strphases, urlindexes);
		if(strphases.isEmpty())
			return str;
		StringBuilder sb = new StringBuilder("<T t=\"\"></T>");
		for(int i = 0 ; i < strphases.size(); i++) {
			String phase = strphases.get(i);
			if(urlindexes.contains(i))
				sb.append("<Http t=\"").append(phase).append("\" address=\"").append(phase).append("\"></Http>");
			else
				sb.append("<T t=\"").append(phase).append("\"></T>");
		}
		return sb.toString();
	}
	
	/**
	 * 将map的key和value拼接成字符串，如：key1,value1;key2,value2;.....
	 * <li> 不会返回Null
	 * @author yuhongyong
	 */
	public static <K,V> String map2String(Map<K,V> map) {
		if (null == map || map.isEmpty()) return "";
		
		StringBuilder result = new StringBuilder();
		int size = map.size();
		int count = 0;
		for (Entry<K,V> e : map.entrySet())
		{
			if (e==null) continue;
			result.append(e.getKey().toString()).append(",").append(e.getValue().toString());
			if (count < size-1)// 最后一项不加;
				result.append(";");
			count ++;
		}
		return result.toString();
	}
	
	/**
	 * 中括号 在json中安全模式
	 * 
	 * @param str
	 * @return
	 */
	public static String tryBracketEncode(String str) {
		if(str != null) {
			str = str.replaceAll("\\[(.*?)\\]", "\\\\\\[$1\\\\\\]");
		}
		return str;
	}
	
	/**
	 * 中括号 在json中安全模式
	 * 
	 * @param str
	 * @return
	 */
	public static String tryBracketDecode(String str) {
		if(str != null) {
			str = str.replaceAll("\\\\\\[(.*?)\\\\\\]", "\\[$1\\]");
		}
		return str;
	}
	
	public static String formatString(String sTargetStr, String... params) {
        if (null == sTargetStr || sTargetStr.isEmpty())
            return "";

        for (int i = params.length - 1; i >= 0; i--) {
            if (params[i] == null)
                continue;

            int j = i + 1;
            sTargetStr = sTargetStr.replaceAll("%" + Integer.toString(j), params[i]);
        }
        return sTargetStr;
    }

}
