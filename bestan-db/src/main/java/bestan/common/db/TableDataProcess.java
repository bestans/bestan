package bestan.common.db;

import org.rocksdb.RocksDBException;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

/**
 * @author yeyouhuan
 *
 */
public class TableDataProcess {
	private DataProcess process;
	TableDataProcess(TableDataType type) {
		process = type.newProcess();
	}
	
	public byte[] getBytes(Object t) throws RocksDBException {
		return process.getBytes(t);
	}
	
	public Object convert(byte[] data) throws RocksDBException {
		return process.convert(data);
	}
	
	public void setMessageInstance(Message message) {
		process.setMessageInstance(message);
	}
	
	public static enum TableDataType {
		BOOL(new BooleanProcess()),
		INT(new IntProcess()),
		LONG(new LongProcess()),
		STRING(new StringProcess()),
		MESSAGE(new MessageProcess());
		
		private DataProcess process;
		TableDataType(DataProcess process) {
			this.process = process;
		}
		public DataProcess newProcess() {
			return process.newProcess();
		}
	}
	
	static abstract class DataProcess {
		protected Class<?> dataClass;
		public DataProcess(Class<?> dataClass) {
			this.dataClass = dataClass;
		}
		protected void checkObjectClass(Object t) throws RocksDBException {
			if (dataClass != t.getClass())
				throw new RocksDBException("invalid data type:expected " + dataClass + ",real " + t.getClass());
		}
		byte[] getBytes(Object t) throws RocksDBException {
			checkObjectClass(t);
			return decode(t);
		}
		protected abstract byte[] decode(Object t) throws RocksDBException;
		abstract Object convert(byte[] data) throws RocksDBException;
		abstract DataProcess newProcess();
		void setMessageInstance(Message message) { 
			
		}
	}
	
	static class IntProcess extends DataProcess {
		public IntProcess() {
			super(Integer.class);
		}

		@Override
		protected byte[] decode(Object t) throws RocksDBException {
			return Ints.toByteArray((Integer)t);
		}

		@Override
		Object convert(byte[] data) throws RocksDBException {
			return Ints.fromByteArray(data);
		}

		@Override
		DataProcess newProcess() {
			// TODO Auto-generated method stub
			return new IntProcess();
		}
	}
	
	static class LongProcess extends DataProcess {

		public LongProcess() {
			super(Long.class);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected byte[] decode(Object t) throws RocksDBException {
			checkObjectClass(t);
			return Longs.toByteArray((Long)t);
		}

		@Override
		Object convert(byte[] data) throws RocksDBException {
			return Longs.fromByteArray(data);
		}

		@Override
		DataProcess newProcess() {
			return new LongProcess();
		}
		
	}
	
	static class StringProcess extends DataProcess {

		public StringProcess() {
			super(String.class);
		}

		@Override
		protected byte[] decode(Object t) throws RocksDBException {
			checkObjectClass(t);
			return ((String)t).getBytes();
		}

		@Override
		Object convert(byte[] data) throws RocksDBException {
			return new String(data);
		}

		@Override
		DataProcess newProcess() {
			// TODO Auto-generated method stub
			return new StringProcess();
		}
		
	}
	
	static class MessageProcess extends DataProcess {
		private Message message;
		
		public MessageProcess() {
			super(null);
		}

		@Override
		protected byte[] decode(Object t) throws RocksDBException {
			return ((Message)t).toByteArray();
		}

		@Override
		Object convert(byte[] data) throws RocksDBException {
			// TODO Auto-generated method stub
			try {
				return message.newBuilderForType().mergeFrom(data).build();
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				throw new RocksDBException("parse message failed:error=" + e.getMessage() + ",message=" + message.getClass().getSimpleName());
			}
		}

		@Override
		void setMessageInstance(Message message) {
			this.message = message;
			this.dataClass = message.getClass();
		}

		@Override
		DataProcess newProcess() {
			return new MessageProcess();
		}
	}
		
	static class BooleanProcess extends DataProcess {

		public BooleanProcess() {
			super(Boolean.class);
		}

		@Override
		protected byte[] decode(Object t) throws RocksDBException {
			byte[] value = { (byte)((Boolean)t ? 1 : 0) };
			return value;
		}

		@Override
		Object convert(byte[] data) throws RocksDBException {
			if (data == null || data.length != 1)
				throw new RocksDBException("parse boolean failed");
			
			return data[0] == (byte)1 ? true : false;
		}

		@Override
		DataProcess newProcess() {
			return new BooleanProcess();
		}
		
	}
}
