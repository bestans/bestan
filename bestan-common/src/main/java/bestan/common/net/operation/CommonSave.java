package bestan.common.net.operation;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import bestan.common.protobuf.Proto;
import bestan.common.protobuf.Proto.DBCommonData;

/**
 * @author yeyouhuan
 *
 */
public class CommonSave {
	private Proto.CommonSave.Builder saveBuilder = Proto.CommonSave.newBuilder();
	
	public CommonSave(String tableName, Object key, Object value) {
		saveBuilder.setKey(toBuider(key));
		saveBuilder.setValue(toBuider(value));
		saveBuilder.setTableName(ByteString.copyFromUtf8(tableName));
	}
	
	public Proto.CommonSave.Builder getBuilder() {
		return saveBuilder;
	}
	
	public static TableDataType getTableDataType(Class<?> cls) {
		if (cls.equals(Integer.class)) {
			return TableDataType.INT;
		}
		if (cls.equals(Long.class)) {
			return TableDataType.LONG;
		}
		if (cls.equals(Message.class)) {
			return TableDataType.MESSAGE;
		}
		if (cls.equals(Boolean.class)) {
			return TableDataType.BOOL;
		}
		if (cls.equals(String.class)) {
			return TableDataType.STRING;
		}
		return null;
	}
	public static DBCommonData.Builder toBuider(Object t) {
		return getTableDataType(t.getClass()).convertPB(t);
	}
}
