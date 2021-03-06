package bestan.common.net.operation;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import bestan.common.message.MessageFactory;
import bestan.common.protobuf.Proto.DBCommonData;
import bestan.common.protobuf.Proto.DBCommonData.DATA_TYPE;

/**
 * @author yeyouhuan
 *
 */
public enum TableDataType {
	BOOL(new BooleanProcess()),
	INT(new IntProcess()),
	LONG(new LongProcess()),
	STRING(new StringProcess()),
	MESSAGE(new MessageProcess());
	
	private DataProcess process;
	TableDataType(DataProcess process) {
		this.process = process;
	}
	
	public DataProcess getProcess(Message instance) {
		return process.getProcess(instance);
	}
	
	public DBCommonData.Builder convertPB(Object t) {
		return process.convertPB(t);
	}
	
	public Object convert(byte[] data, int messageId) {
		return process.convert(data, messageId);
	}

	public static Object convertObject(DBCommonData data) {
		var dataType = getTableDataType(data.getDataType());
		if (null == dataType) {
			return null;
		}
		return dataType.convert(data.getData().toByteArray(), data.getDataMessageID());
	}
	
	public static TableDataType getTableDataType(Class<?> cls) {
		if (cls.equals(Integer.class)) {
			return TableDataType.INT;
		}
		if (cls.equals(Long.class)) {
			return TableDataType.LONG;
		}
		if (Message.class.isAssignableFrom(cls)) {
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
	
	public static TableDataType getTableDataType(DATA_TYPE dataType) {
		switch (dataType) {
		case BOOL:
			return TableDataType.BOOL;
		case INT:
			return TableDataType.INT;
		case LONG:
			return TableDataType.LONG;
		case MESSAGE:
			return TableDataType.MESSAGE;
		case STRING:
			return TableDataType.STRING;
		default:
			break;
		}
		return null;
	}

	public static abstract class DataProcess {
		protected Class<?> dataClass;
		public DataProcess(Class<?> dataClass) {
			this.dataClass = dataClass;
		}
		protected void checkObjectClass(Object t) {
			if (dataClass != t.getClass())
				throw new RuntimeException("invalid data type:expected " + dataClass + ",real " + t.getClass());
		}
		public byte[] getBytes(Object t) {
			checkObjectClass(t);
			return decode(t);
		}
		protected abstract byte[] decode(Object t);
		public abstract Object convert(byte[] data);
		public Object convert(byte[] data, Class<? extends Message> cls) {
			return convert(data);
		}
		public Object convert(byte[] data, int messageId) {
			return convert(data);
		}
		
		public DataProcess getProcess(Message instance) {
			return this;
		}
		
		public DBCommonData.Builder convertPB(Object t) {
			var builder = DBCommonData.newBuilder();
			builder.setData(ByteString.copyFrom(decode(t)));
			builder.setDataType(getDataType());
			builder.setDataMessageID(getDataMessageID(t));
			return builder;
		}
		protected abstract DATA_TYPE getDataType();
		protected int getDataMessageID(Object t) {
			return 0;
		}
	}
	
	static class IntProcess extends DataProcess {
		public IntProcess() {
			super(Integer.class);
		}

		@Override
		protected byte[] decode(Object t) {
			return Ints.toByteArray((Integer)t);
		}

		@Override
		public Object convert(byte[] data) {
			return Ints.fromByteArray(data);
		}

		@Override
		protected DATA_TYPE getDataType() {
			// TODO Auto-generated method stub
			return DATA_TYPE.INT;
		}
	}
	
	static class LongProcess extends DataProcess {

		public LongProcess() {
			super(Long.class);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected byte[] decode(Object t) {
			checkObjectClass(t);
			return Longs.toByteArray((Long)t);
		}

		@Override
		public Object convert(byte[] data) {
			return Longs.fromByteArray(data);
		}

		@Override
		protected DATA_TYPE getDataType() {
			return DATA_TYPE.LONG;
		}
	}
	
	static class StringProcess extends DataProcess {

		public StringProcess() {
			super(String.class);
		}

		@Override
		protected byte[] decode(Object t) {
			checkObjectClass(t);
			return ((String)t).getBytes();
		}

		@Override
		public Object convert(byte[] data) {
			return new String(data);
		}

		@Override
		protected DATA_TYPE getDataType() {
			return DATA_TYPE.STRING;
		}
		
	}
	
	static class MessageInstanceProcess extends DataProcess {
		private Message message;

		public MessageInstanceProcess(Message message) {
			super(message.getClass());
			this.message = message;
		}

		@Override
		protected byte[] decode(Object t) {
			return ((Message)t).toByteArray();
		}

		@Override
		public Object convert(byte[] data) {
			// TODO Auto-generated method stub
			try {
				return message.newBuilderForType().mergeFrom(data).build();
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException("parse message failed:error=" + e.getMessage() + ",message=" + message.getClass().getSimpleName());
			}
		}

		@Override
		protected DATA_TYPE getDataType() {
			return DATA_TYPE.MESSAGE;
		}
		
		@Override
		protected int getDataMessageID(Object t) {
			// TODO Auto-generated method stub
			return MessageFactory.getMessageIndex((Message)t);
		}
	}
	static class MessageProcess extends DataProcess {
		
		public MessageProcess() {
			super(null);
		}

		@Override
		protected void checkObjectClass(Object t) {
			
		}
		
		@Override
		protected byte[] decode(Object t) {
			return ((Message)t).toByteArray();
		}
		
		@Override
		public Object convert(byte[] data, Class<? extends Message> cls) {
			var instance = MessageFactory.getMessageInstance(cls);

			try {
				return instance.newBuilderForType().mergeFrom(data).build();
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException("parse message failed:error=" + e.getMessage() + ",message=" + instance.getClass().getSimpleName());
			}
		}
		
		@Override
		public Object convert(byte[] data, int messageId) {
			var instance = MessageFactory.getMessageInstance(messageId);

			try {
				return instance.newBuilderForType().mergeFrom(data).build();
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException("parse message failed:error=" + e.getMessage() + ",message=" + instance.getClass().getSimpleName());
			}
		}
		
		@Override
		public DataProcess getProcess(Message instance) {
			return new MessageInstanceProcess(instance);
		}

		@Override
		public Object convert(byte[] data) {
			return null;
		}

		@Override
		protected DATA_TYPE getDataType() {
			// TODO Auto-generated method stub
			return DATA_TYPE.MESSAGE;
		}

		@Override
		protected int getDataMessageID(Object t) {
			// TODO Auto-generated method stub
			return MessageFactory.getMessageIndex((Message)t);
		}
	}
		
	static class BooleanProcess extends DataProcess {

		public BooleanProcess() {
			super(Boolean.class);
		}

		@Override
		protected byte[] decode(Object t) {
			byte[] value = { (byte)((Boolean)t ? 1 : 0) };
			return value;
		}

		@Override
		public Object convert(byte[] data) {
			if (data == null || data.length != 1)
				throw new RuntimeException("parse boolean failed");
			
			return data[0] == (byte)1 ? true : false;
		}

		@Override
		protected DATA_TYPE getDataType() {
			// TODO Auto-generated method stub
			return DATA_TYPE.BOOL;
		}
		
	}
}
