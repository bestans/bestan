package bestan.common.net.operation;

import com.google.protobuf.ByteString;

import bestan.common.protobuf.Proto;

/**
 * @author yeyouhuan
 *
 */
public class CommonLoad {

	private Proto.CommonLoad.Builder loadBuilder = Proto.CommonLoad.newBuilder();
	
	public CommonLoad(String tableName, Object key) {
		loadBuilder.setKey(CommonSave.toBuider(key));
		loadBuilder.setTableName(ByteString.copyFromUtf8(tableName));
	}
	

	public Proto.CommonLoad.Builder getBuilder() {
		return loadBuilder;
	}
}
