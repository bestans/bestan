package bestan.common.offline;

import java.util.Map;

public interface IOfflineHash {
	public void convert(Map<String, String> convertMap);
	public Map<String, String> transToHashMap();
}
