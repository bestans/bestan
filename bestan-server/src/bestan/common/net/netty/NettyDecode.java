package bestan.common.net.netty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import bestan.common.log.LogManager;
import bestan.common.net.message.MessageFactory;
import bestan.common.net.message.ProtoMessagePack;
import bestan.common.util.Global;
import bestan.common.util.Utility;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class NettyDecode {
	
	/** the logger instance. */
	private static final bestan.common.log.Logger logger = LogManager.getLogger(NettyDecode.class);

	/**
	 * Old Package
	 * |len|msgid|msg|
	 * |4  |4    |xxx|
	 * |header = len | body = msgid + msg |
	 * 
	 * Package
	 * |len | operation | synckey | msgid | msg |
	 * | 4  | 1         | 4       | 4     | xxx |
	 * |header = len | body = operation + synckey + msgid + msg |
	 */
	private static final byte G_OPERATION_SYNCKEY_VALUE = 0x01;
	private static final int G_OPERATION_SIZE = 1;
	private static final int G_SYNCKEY_SIZE = 4;
	
	private static final int G_MSGID_SIZE = 4;
	private static final int G_HEADER_SIZE = 4;
	
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
		public ProtoMessagePack build(Channel ctx) throws IOException {
			int length = 0;
			for (byte[] p : parts) {
				length += p.length;
			}
	
			// Check Header Size
			if (length < G_HEADER_SIZE) {
				return null;
			}
			
			// read size of message
			int size = readSizeOfMessage();
			
			// check size
			if(size < G_MSGID_SIZE + G_OPERATION_SIZE) {
				logger.error("ProtoDebug:Message format error, todo: need modified quickly use new packet format");
				throw new IOException("Message format error, todo: need modified quickly use new packet format");
			}
			
			/*
			 * If length is bigger than size that means that two messages on
			 * a row... so we need to run until the end of the first one.
			 */
			if (length - G_HEADER_SIZE < size) {
				/*
				 * Still missing parts, let's wait 
				 */
				return null;
			}

			// 这个时候是整个包全部读取完了 
			// 1. 至少可以读取 G_MSGID_SIZE + G_OPERATION_SIZE 的数据
			// read Opration byte
			byte operationType = readByteOfMessage(G_HEADER_SIZE);
			// check sync key
			int syncKey = Global.INVALID_VALUE;
			int readMsgIdIndex = G_HEADER_SIZE + G_OPERATION_SIZE;
			int deltaMsgSize = G_MSGID_SIZE + G_OPERATION_SIZE;
			if(G_OPERATION_SYNCKEY_VALUE == operationType) {
				// check size after sync
				if(size < G_MSGID_SIZE + G_OPERATION_SIZE + G_SYNCKEY_SIZE) {
//					logger.debug("Empty Message will be Drop");
					logger.error("ProtoDebug:Message format error, todo: need modified quickly use new packet format");
					throw new IOException("Message format error, todo: need modified quickly use new packet format");				
				}
				syncKey = readIntOfMessage(G_HEADER_SIZE + G_OPERATION_SIZE);
				readMsgIdIndex += G_SYNCKEY_SIZE;
				deltaMsgSize += G_SYNCKEY_SIZE;
			}

			// set recv one packet size
			int remaining = size + G_HEADER_SIZE;
			byte[] data = new byte[remaining];

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
			
			int nMsgId = Utility.byteArrayToInt(data, readMsgIdIndex);
			byte[] msgBytes = new byte[size-deltaMsgSize];
			System.arraycopy(data, readMsgIdIndex+G_MSGID_SIZE, msgBytes, 0, size-deltaMsgSize);
			ProtoMessagePack msg = msgFactory.getMessagePack(nMsgId, msgBytes);

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

			return Utility.byteArrayToInt(size, 0);
		}
		
		/**
		 * reads the size of the message. Important: It expects at least 4 bytes in the buffer.
		 *
		 * @return int if failed ret 0
		 */
		private int readIntOfMessage(int index) {
			byte[] size = new byte[4];
			int offset = 0;
			int len = 0;
			
			loops:
			for (byte[] part : parts) {
				int oldLen = len;
				len += part.length;
				if(index >= len) {
					continue;
				}
				
				int beginIdx = (index > oldLen) ? (index - oldLen) : 0;
				for (int i = beginIdx; i < part.length; i++) {
					size[offset] = part[i];

					// if we have read four bytes, break
					if (offset == 3) {
						break loops;
					}
					offset++;
				}
			}

			return Utility.byteArrayToInt(size, 0);
		}
		
		/**
		 * reads the size of the message.
		 *
		 * @return byte if failed ret 0x00
		 */
		private byte readByteOfMessage(int index) {
			int len = 0;
			for (byte[] part : parts) {
				int oldLen = len;
				len += part.length;
				if(index >= len) {
					continue;
				}
				
				return part[index - oldLen];
			}
			
			return 0x00;
		}
	}

	/** We map each channel with the sent content */
	private Map<Channel, MessageParts> content;

	/** MessageFactory */
	private MessageFactory msgFactory;

	/** singleton instance */
	private static NettyDecode instance;

	/**
	 * Returns an unique instance of decoder
	 *
	 * @return an unique instance of decoder
	 */
	public static NettyDecode get() {
		if (instance == null) {
			instance = new NettyDecode();
		}

		return instance;
	}

	public Object readBlobObject(java.sql.Blob blob, int nMsgId, boolean gzip) throws SQLException, IOException {
		if(null == blob) {
			return null;
		}
		
	    InputStream input = blob.getBinaryStream();
	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    // set read buffer size 64 k limit
	    byte[] rb = new byte[1024];
	    int ch = 0;
	    // process blob
	    while ((ch = input.read(rb)) != Global.INVALID_VALUE) {
	    	output.write(rb, 0, ch);
	    }
	    byte[] content = output.toByteArray();
	    input.close();
	    output.close();
	    
	    // gzip
//	    ByteArrayInputStream inStream = new ByteArrayInputStream(content);	    
//	    InflaterInputStream szlib = new InflaterInputStream(inStream, new Inflater());
//	    InputSerializer inputSerializer = new InputSerializer(szlib);
	    
	    if(content.length < 0) {
	    	return null;
	    }
	    
	    return msgFactory.getMessage(nMsgId, content);
    }
	
	public Object readBlobObjectByBase64(java.sql.Blob blob, int nMsgId, boolean gzip) throws SQLException, IOException {
		if(null == blob) {
			return null;
		}
		
	    InputStream input = blob.getBinaryStream();
	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    // set read buffer size 64 k limit
	    byte[] rb = new byte[1024];
	    int ch = 0;
	    // process blob
	    while ((ch = input.read(rb)) != Global.INVALID_VALUE) {
	    	output.write(rb, 0, ch);
	    }
	    byte[] content = output.toByteArray();
	    input.close();
	    output.close();
	    
	    // gzip
//	    ByteArrayInputStream inStream = new ByteArrayInputStream(content);	    
//	    InflaterInputStream szlib = new InflaterInputStream(inStream, new Inflater());
//	    InputSerializer inputSerializer = new InputSerializer(szlib);
	    
	    if(content.length < 0) {
	    	return null;
	    }
	    
	    return msgFactory.getMessage(nMsgId, bestan.common.util.Base64.decodeBase64(content));
   }
	
	/**
	 * Constructor
	 *
	 */
	private NettyDecode() {
		content = new HashMap<Channel, MessageParts>();
		msgFactory = MessageFactory.getFactory();
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
	public List<ProtoMessagePack> decode(Channel channel, ByteBuf in) throws IOException {
		MessageParts buffers = content.get(channel);

		//logger.error("Pring Msg:" + ByteBufUtil.hexDump(in));
		int dataLen = in.readableBytes();
//		logger.debug("Message dataLen "+dataLen);
	
		if (buffers == null) {
			/* First part of the message */
			/*
			 * We need to be *sure* that 4 bytes are at least to
			 * be received...
			 */
			if(dataLen < G_HEADER_SIZE) {
				logger.error("ProtoDebug:Message is too short. Missing mandatory fields.");
				throw new IOException("Message is too short. Missing mandatory fields.");
			}
			
//			if(ByteOrder.BIG_ENDIAN == in.order()) {
//				logger.debug("Message is BigEndian");
//			} else {
//				logger.debug("Message is LittleEndian");
//			}
//			System.out.println("Test Print A MsgSize:" + ByteBufUtil.hexDump(in, 0, 4));
//			byte[] test = new byte[4];
//			in.getBytes(0, test, 0, 4);
//			System.out.println("Message Test Size:" + Utility.byteArrayToInt(test, 0));
//			System.out.println("Test Print L MsgSize:" + ByteBufUtil.hexDump(in, 0, 4));
			
			int size = in.readInt();
//			logger.debug("Message size "+size);
			if(size < 0) {
				logger.error("ProtoDebug:Message size is negative. Message ignored.");
				throw new IOException("Message size is negative. Message ignored.");
			}

			if (dataLen == size + G_HEADER_SIZE) {
				/* If we have the full data build the message */
				if(size < G_MSGID_SIZE + G_OPERATION_SIZE) {
//					logger.debug("Empty Message will be Drop");
					return null;
				}
				
				// read Opration byte
				byte operationType = in.getByte(G_HEADER_SIZE);
				
				// check sync key
				int syncKey = Global.INVALID_VALUE;
				int readMsgIdIndex = G_HEADER_SIZE + G_OPERATION_SIZE;
				int deltaMsgSize = G_MSGID_SIZE + G_OPERATION_SIZE;
				if(G_OPERATION_SYNCKEY_VALUE == operationType) {
					// check size after sync
					if(size < G_MSGID_SIZE + G_OPERATION_SIZE + G_SYNCKEY_SIZE) {
//						logger.debug("Empty Message will be Drop");
						return null;
					}
					syncKey = in.getInt(G_HEADER_SIZE + G_OPERATION_SIZE);
					readMsgIdIndex += G_SYNCKEY_SIZE;
					deltaMsgSize += G_SYNCKEY_SIZE;
				}
				
				// read msgid
				int nMsgId = in.getInt(readMsgIdIndex);
				
				// get msg
				byte[] msgBytes = new byte[size-deltaMsgSize];
				in.getBytes(readMsgIdIndex + G_MSGID_SIZE, msgBytes, 0, size-deltaMsgSize);
				ProtoMessagePack msg = msgFactory.getMessagePack(nMsgId, msgBytes);
				List<ProtoMessagePack> list = new LinkedList<ProtoMessagePack>();
				list.add(msg);
				return list;
			} else {
//				logger.debug("Message full body missing ("+size+"), waiting for more data ("+dataLen+").");
				/* If there is still data to store it. */
				buffers = new MessageParts();
				content.put(channel, buffers);
			}
		} else {
//			logger.debug("Existing data, trying to complete Message full body");
		}

		byte[] addData = new byte[dataLen];
		in.getBytes(0, addData, 0, dataLen);
		buffers.add(addData);
		List<ProtoMessagePack> list = new LinkedList<ProtoMessagePack>();

		while (!buffers.isEmpty()) {
			ProtoMessagePack msg = buffers.build(channel);

			if (msg != null) {
				list.add(msg);
			} else {
				if (list.isEmpty()) {
					return null;
				}
				return list;
			}
		}
		
		content.remove(channel);

		return list;
	}
}
