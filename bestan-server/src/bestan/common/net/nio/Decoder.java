package bestan.common.net.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import bestan.common.log.LogManager;
import bestan.common.net.message.MessageFactory;
import bestan.common.net.nio.MessagePack;

/**
 * This class decodes a stream of bytes and builds a protobuf message with it.
 * Decoder follows singleton pattern.
 */
public class Decoder {

	/** the logger instance. */
	private static final bestan.common.log.Logger logger = LogManager.getLogger(Decoder.class);

	private static int G_HEADER_SIZE = 4;
	private static int G_MSGID_SIZE = 4;
	/**
	 * This class handles incomplete messages.
	 */
	class MessageParts {	
		public Vector<byte[]> parts;

		public MessageParts() {
			parts = new Vector<byte[]>();
		}

		/**
		 * Adds a new part to complete the message
		 *
		 * @param data data to add
		 */
		public void add(byte[] data) {
			parts.add(data);
		}
		
		public boolean isEmpty() {
			return parts.isEmpty();
		}

		/**
		 * Try to build the message for the channel using the existing parts or
		 * return null if it is not completed yet.
		 *
		 * @param channel SocketChannel to read from
		 * @return Message read message
		 * @throws IOException in case of an input / output error
		 * @throws InvalidVersionException in case the protocol version is not supported
		 */
		@SuppressWarnings("null")
		public MessagePack build(SocketChannel channel) throws IOException {
			int length = 0;
			for (byte[] p : parts) {
				length += p.length;
			}

			// the first 4 bytes are the size
			// ��ʱ��ŵ������ﲻ��ɾ��
			if (length < G_HEADER_SIZE) {
				return null;
			}
			
			int size = readSizeOfMessage();
			
			//��Ҫ�ж�size == G_MSGID_SIZE�����
			if(size <= G_MSGID_SIZE) {
				//��ʱ���ְ�����Ϊ�Ƿ��� ��Ҫdrop�� 
				//��Ҫ�ҵ���һ����ȷ�İ� ����ǰ�������쳣��ȫ������ ��Ҫ��һ�����⴦��
				throw new IOException("Message format error, todo: need modified quickly use new packet format");
			}
			
			/*
			 * If length is bigger than size that means that two messages on
			 * a row... so we need to run until the end of the first one.
			 */
			if (length - G_HEADER_SIZE < size) {
				/*
				 * Still missing parts, let's wait 
				 * ��ʱҲ����ɾ�� ��ŵ�������
				 */
				return null;
			}
			
			byte[] data = new byte[size+G_HEADER_SIZE];
			
			int remaining = size + G_HEADER_SIZE;

			int offset = 0;
			Iterator<byte[]> it=parts.iterator();
			while(it.hasNext()) {
				byte[] p=it.next();
				
				if(remaining-p.length<0) {
					/*
					 * This part completes first message and has stuff from the second one.
					 */
					System.arraycopy(p, 0, data, offset, remaining);
					
					/*
					 * Copy the rest of the array to a new array.					  
					 */
					byte[] rest = new byte[p.length-remaining];
					System.arraycopy(p, remaining, rest, 0, p.length-remaining);
					parts.set(0, rest);

					/*
					 * Stop iterating and process the currently complete message.
					 */
					break;
				} else {
					System.arraycopy(p, 0, data, offset, p.length);
					offset += p.length;
					remaining -=p.length;
					
					it.remove();
				}
			}

			@SuppressWarnings("unused")
			int nMsgId = byteArrayToInt(data, 4);
			byte[] msgBytes = new byte[size-G_MSGID_SIZE];
			System.arraycopy(data, 8, msgBytes, 0, size-G_MSGID_SIZE);
//			MessagePack msg = msgFactory.getMessagePack(nMsgId, msgBytes);
			MessagePack msg = null;
			//Ϊ�˸��Ժ��gameservermanager ��������Ҫ��channel����ȥ �Ժ���ܻ���Ҫ�ɵ� ��Ϊ��������������÷ֿ�����
			msg.setChannel(channel);
			return msg;
		}

		/**
		 * reads the size of the message. Important: It expects at least 4 bytes in the buffer.
		 *
		 * @return size
		 */
		private int readSizeOfMessage() {
			byte[] size = new byte[4];
			int offset = 0;

			loops:
			for (byte[] part : parts) {
				for (int i = 0; i < part.length; i++) {
					size[offset] = part[i];

					// if we have read four bytes, break
					if (offset == 3) {
						break loops;
					}
					offset++;
				}
			}

			return getSizeOfMessage(size);
		}
	}

	/** We map each channel with the sent content */
	private Map<SocketChannel, MessageParts> content;

	/** MessageFactory */
	@SuppressWarnings("unused")
	private MessageFactory msgFactory;

	/** singleton instance */
	private static Decoder instance;

	/**
	 * Returns an unique instance of decoder
	 *
	 * @return an unique instance of decoder
	 */
	public static Decoder get() {
		if (instance == null) {
			instance = new Decoder();
		}

		return instance;
	}

	/**
	 * Constructor
	 *
	 */
	private Decoder() {
		content = new HashMap<SocketChannel, MessageParts>();
		msgFactory = MessageFactory.getFactory();
	}

	/**
	 * Removes any pending data from a connection
	 *
	 * @param channel
	 *            the channel to clear data from.
	 */
	public void clear(SocketChannel channel) {
		content.remove(channel);
	}
	
	private static int byteArrayToInt(byte[] b, int pos) {  
	    return   b[pos+3] & 0xFF |  
	            (b[pos+2] & 0xFF) << 8 |  
	            (b[pos+1] & 0xFF) << 16 |  
	            (b[pos] & 0xFF) << 24;  
	}  
	  
	@SuppressWarnings("unused")
	private byte[] intToByteArray(int a) {  
	    return new byte[] {  
	        (byte) ((a >> 24) & 0xFF),  
	        (byte) ((a >> 16) & 0xFF),     
	        (byte) ((a >> 8) & 0xFF),     
	        (byte) (a & 0xFF)  
	    };  
	}
	
	//by lxw �޸� windows Э���С�� ��Ҫȷ���Ƿ�ͨ��JVM???
	static int getSizeOfMessage(byte[] data) {
//		return (data[0] & 0xFF) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xFF) << 16)
//        + ((data[3] & 0xFF) << 24);
		
		return byteArrayToInt(data, 0);
	}
	
	/**
	 * Decodes a message from a stream of bytes received from channel
	 *
	 * @param channel
	 *            the socket from where data was received
	 * @param data
	 *            the data received
	 * @return a message or null if it was not possible
	 * @throws IOException
	 *             if there is a problem building the message
	 * @throws InvalidVersionException
	 *             if the message version mismatch the expected version
	 */
	@SuppressWarnings({ "unused", "null" })
	public List<MessagePack> decode(SocketChannel channel, byte[] data) throws IOException {
		MessageParts buffers = content.get(channel);

		if (buffers == null) {
			/* First part of the message */
			/*
			 * We need to be *sure* that 4 bytes are at least to
			 * be received...
			 * todo: ����û�л����ǰ���°������Сreadʱ С��4�ֽڽ��ᶪ�� �Ƿ���Ҫ���棿����
			 */
			if(data.length < G_HEADER_SIZE) {
				throw new IOException("Message is too short. Missing mandatory fields.");
			}
			
			int size = getSizeOfMessage(data);

			if(size<0) {
				throw new IOException("Message size is negative. Message ignored.");
			}

			if (data.length == size + G_HEADER_SIZE) {
				/* If we have the full data build the message */
				//�������С��msgid�ĳ������������� ��˵����һ����һ���Ƿ��İ�
				if(size <= G_MSGID_SIZE) {
					logger.debug("Empty Message will be Drop");
					return null;
				}
				
				//��ȡ��ID
				int nMsgId = byteArrayToInt(data, 4);
				
				byte[] msgBytes = new byte[size-G_MSGID_SIZE];
				System.arraycopy(data, 8, msgBytes, 0, size-G_MSGID_SIZE);
//				MessagePack msg = msgFactory.getMessagePack(nMsgId, msgBytes);
				MessagePack msg = null;
				msg.setChannel(channel); //�Ժ�Ӧ���Ż�messagepack �Լ� gameservermanager
				
				List<MessagePack> list=new LinkedList<MessagePack>();
				list.add(msg);
				
				return list;
			} else {
				logger.debug("Message full body missing ("+size+"), waiting for more data ("+data.length+").");
				/* If there is still data to store it. */
				buffers = new MessageParts();
				content.put(channel, buffers);
			}
		} else {
			logger.debug("Existing data, trying to complete Message full body");
		}

		buffers.add(data);
		List<MessagePack> list = new LinkedList<MessagePack>();

		while (!buffers.isEmpty()) {
			MessagePack msg = buffers.build(channel);

			if (msg != null) {
				list.add(msg);
			} else {
				if (list.isEmpty()) {
					return null;
				}
				return list;
			}
		}
		
		//��ʱ������ӵİ�ȫ�������겢���޻�����Ҫ���map
		content.remove(channel);

		return list;
	}
}
