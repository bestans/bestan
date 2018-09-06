package bestan.common.db;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import bestan.common.message.MessageFactory;

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
		
		public DataProcess getProcess(Message instance) {
			return this;
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
		public DataProcess getProcess(Message instance) {
			return new MessageInstanceProcess(instance);
		}

		@Override
		public Object convert(byte[] data) {
			return null;
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
		
	}
}
