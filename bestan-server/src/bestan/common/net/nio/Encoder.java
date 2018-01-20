package bestan.common.net.nio;

import bestan.common.log.LogManager;
import bestan.common.util.GZipUtil;

/**
 * This class encodes a protobuf Message as a stream of bytes.
 * Encoder follows singleton pattern.
 * 
 * @Warn: There is no synckey but decode have sychkey {@link NettyDecode}
 * Package
 * |len | operation | msgid | msg |
 * | 4  | 1         | 4     | xxx |
 * |header = len | body = operation + msgid + msg |
 */
public class Encoder {
	private static final int MSGID_SIZE = 4;
	private static final int HEADER_SIZE = 4;
	private static final int OPERATION_SIZE = 1;
	private static final int EXCEPT_MSG_SIZE = HEADER_SIZE + OPERATION_SIZE + MSGID_SIZE;
	
	private static bestan.common.log.Logger logger = LogManager.getLogger(Encoder.class);
	
	/**
	 * Constructor
	 */
	private Encoder() {
		// hide constructor, this is a Singleton
	}

	/** the singleton instance */
	private static Encoder instance;

	/**
	 * Returns an unique instance of encoder
	 *
	 * @return an unique instance of encoder
	 */
	public static Encoder get() {
		if (instance == null) {
			instance = new Encoder();
		}

		return instance;
	}

	/**
	 * This method uses the protocol to encode a protobuf GeneratedMessage as a stream of
	 * bytes.
	 * format: LEN[4] + MSGID[4] + protobufstream
	 * 
	 * @param msg
	 *            The message to encode
	 * @return a byte array
	 * @throws IOException
	 *             if there is any error encoding the message.
	 */
	public byte[] encode(com.google.protobuf.GeneratedMessage msg, int msgId, byte operation) throws Exception {
		int size = 0;		
		
		byte[] tmpMsg = msg.toByteArray();
		byte[] data = new byte[tmpMsg.length + EXCEPT_MSG_SIZE];
		
		if(logger.isDebugEnabled()) {
			//logger.debug("SendMsg ID:" + msgId + " size:" + tmpMsg.length + " msg:" + msg.toString());
			logger.debug("SendMsg ID:" + msgId + " size:" + tmpMsg.length + " compress size:" + GZipUtil.compress(tmpMsg).length);
		}
		
		// copy msg
		System.arraycopy(tmpMsg, 0, data, EXCEPT_MSG_SIZE, tmpMsg.length);

		// write len
		size = data.length - 4;
		data[3] = (byte) ((size >> 0) & 255);
		data[2] = (byte) ((size >> 8) & 255);
		data[1] = (byte) ((size >> 16) & 255);
		data[0] = (byte) ((size >> 24) & 255);
		
		// write operation
		data[4] = operation;
		
		// write msgid
		data[8] = (byte) ((msgId >> 0) & 255);
		data[7] = (byte) ((msgId >> 8) & 255);
		data[6] = (byte) ((msgId >> 16) & 255);
		data[5] = (byte) ((msgId >> 24) & 255);
		
		return data;
	}
}
