package bestan.common.log;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bestan.common.datastruct.Pair;

public abstract class LogInterface {
	private static final Logger logger = LogManager.getLogger(LogInterface.class);
	public static final String LOG_DATA_SPLIT = ":";
	public static final String LOG_DATA_SUB_SPLIT = ";";
	protected String[] data;
	
	public LogInterface() {
		data = new String[getLogInfoCount()];
	}

	// 获取log类型
	abstract public String getLogType();
	// 获取log信息类型
	abstract public String[] getLogInfoType();
	// 获取log信息数量
	abstract public int getLogInfoCount();

	public void setDataByIndex(int index, String dataValue) {
		if (index < 0 || index >= data.length){
			return;
		}
		data[index] = dataValue;
	}

	public void setDataByIndex(int index, int dataValue) {
		setDataByIndex(index, String.valueOf(dataValue));
	}
	
	public void setLongDataByIndex(int index, List<Long> valueList) {
		if (index < 0 || index >= data.length || valueList == null){
			return;
		}

		StringBuffer result = new StringBuffer();
		boolean isFirst = true;;
		for (Long value : valueList){
			if (isFirst){
				isFirst = false;
			} else{
				result.append(LOG_DATA_SUB_SPLIT);
			}
			result.append(value);
		}
		data[index] = result.toString();
	}

	public void setListDataByIndex(int index, List<List<Integer>> valueList) {
		if (index < 0 || index >= data.length || valueList == null){
			return;
		}

		StringBuffer result = new StringBuffer();
		boolean isFirst = true;
		for (List<Integer> value : valueList){
			if (isFirst){
				isFirst = false;
			} else{
				result.append(LOG_DATA_SUB_SPLIT);
			}
			
			if (value != null){
				for (int i = 0; i < value.size(); ++i){
					if (i != 0){
						result.append(",");
					}
					result.append(value.get(i));
				}
			}
		}
		data[index] = result.toString();
	}

	public void setThreeDataByIndex(int index, Map<Integer, Pair<Integer, Integer>> valueList) {
		if (index < 0 || index >= data.length || valueList == null){
			return;
		}

		StringBuffer result = new StringBuffer();
		boolean isFirst = true;
		for (Entry<Integer, Pair<Integer, Integer>> value : valueList.entrySet()){
			if (isFirst){
				isFirst = false;
			} else{
				result.append(LOG_DATA_SUB_SPLIT);
			}

			if (value != null && value.getValue() != null){
				result.append(value.getKey());
				result.append(",");
				result.append(value.getValue().first());
				result.append(",");
				result.append(value.getValue().second());
			}
		}
		data[index] = result.toString();
	}
	
	public void setFloatDataByIndex(int index, List<Float> valueList) {
		if (index < 0 || index >= data.length || valueList == null){
			return;
		}

		StringBuffer result = new StringBuffer();
		boolean isFirst = true;;
		for (float value : valueList){
			if (isFirst){
				isFirst = false;
			} else{
				result.append(LOG_DATA_SUB_SPLIT);
			}
			result.append(value);
		}
		data[index] = result.toString();
	}
	
	public void setDataByIndex(int index, List<Integer> valueList) {
		if (index < 0 || index >= data.length || valueList == null){
			return;
		}

		StringBuffer result = new StringBuffer();
		boolean isFirst = true;;
		for (int value : valueList){
			if (isFirst){
				isFirst = false;
			} else{
				result.append(LOG_DATA_SUB_SPLIT);
			}
			result.append(value);
		}
		data[index] = result.toString();
	}

	public String format(){
		StringBuffer result = new StringBuffer();
		String[] logInfoType = getLogInfoType();
		if (data != null && logInfoType != null){
			for (int i = 0; i < data.length && i < logInfoType.length; ++i){
				if (i != 0){
					result.append(LOG_DATA_SPLIT);	
				}
				result.append(logInfoType[i] + "=" + data[i]);
			}
		}
		return result.toString();
	}

	public void log(){
		logger.info(getLogType() + ":" + format());
	}
}
