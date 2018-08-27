package bestan.common.net.operation;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import bestan.common.message.MessageFactory;
import bestan.common.protobuf.Proto;
import bestan.common.protobuf.Proto.DBCommonKey;

/**
 * @author yeyouhuan
 *
 */
public class CommonSave {
	private Proto.CommonSave.Builder saveBuilder = Proto.CommonSave.newBuilder();
	private DBCommonKey.Builder builder = DBCommonKey.newBuilder();
	
	private void initBuilder(String tableName, Message value) {
		builder.setTableName(ByteString.copyFrom(tableName.getBytes()));
		builder.setValueMessageId(MessageFactory.getMessageIndex(value));
		
		saveBuilder.setKey(builder);
		saveBuilder.setValue(value.toByteString());
	}
	
	public CommonSave(String tableName, int key, Message value) {
		builder.setKeyType(DBCommonKey.KEY_TYPE.INT);
		builder.setIntKey(key);
		initBuilder(tableName, value);
	}
	
	public CommonSave(String tableName, Long key, Message value) {
		builder.setKeyType(DBCommonKey.KEY_TYPE.LONG);
		builder.setLongKey(key);
		initBuilder(tableName, value);
	}
	
	public CommonSave(String tableName, Message key, Message value) {
		builder.setKeyType(DBCommonKey.KEY_TYPE.MESSAGE);
		builder.setMessagekey(key.toByteString());
		initBuilder(tableName, value);
	}
	
	public Proto.CommonSave.Builder getBuilder() {
		return saveBuilder;
	}
}
