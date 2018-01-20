package bestan.common.net.message;



import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import bestan.common.protobuf.NetBase;
import bestan.common.protobuf.NetBase.BaseMessage;
import bestan.log.GLog;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.google.protobuf.InvalidProtocolBufferException;


/**
 * MessageFactory is the class that is in charge of building the messages from
 * the stream of bytes.
 *
 * MessageFactory follows the singleton pattern.
 *
 */
public class MessageFactory {

	/** the logger instance. */
	private static final Logger logger = GLog.log;

	/** A factory to create messages instance from an integer code. */
	private Map<Integer, Message> factoryArray;
	private Map<Class<? extends Message>, Integer> revertFactoryArray;

	/** 
     *类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 
     *没有绑定关系，而且只有被调用到时才会装载，从而实现了延迟加载。 
     */  
    private static class MessageFactoryHolder{  
        /** 
         * 静态初始化器，由JVM来保证线程安全 
         */  
        private static MessageFactory instance = new MessageFactory();  
    }  
  
    /** 
     *当getInstance方法第一次被调用的时候，它第一次读取 
     *MessageFactoryHolder.instance，导致类得到初MessageFactoryHolder始化；而这个类在装载并被初始化的时候，会初始化它的静 
     *态域，从而创建MessageFactory的实例，由于是静态的域，因此只会在虚拟机装载类的时候初始化一次，并由虚拟机来保证它的线程安全性。 
     *这个模式的优势在于，getFactory方法并没有被同步，并且只是执行一个域的访问，因此延迟初始化并没有增加任何访问成本。 
     */  
    public static MessageFactory getFactory() {  
        return MessageFactoryHolder.instance;  
    }  
    
	/**
	 * Constructor
	 *
	 */
	private MessageFactory() {
		factoryArray = new HashMap<Integer, Message>();
		revertFactoryArray = new HashMap<Class<? extends Message>, Integer>();
	}

	
	/**
	 * 初始化
	 * @param messageRegister
	 * @return
	 */
	public boolean init(IMessageRegister messageRegister) {
		if(null != messageRegister) {
			messageRegister.messageRegister();
		}

		return true;
	}
	
	public void register(int index, Message msgInstance) {
		if(factoryArray.containsKey(index)) {
			logger.error("the key:" + index + " is duplicate");
			return;
		}

		factoryArray.put(index, msgInstance);
		revertFactoryArray.put(msgInstance.getClass(), index);
	}

	
	/**
	 * Returns a object of the right class from a stream of serialized data.
	 *
	 * @param data
	 *            the serialized data
	 * @return a message of the right class
	 * @throws IOException
	 *             in case of problems with the message
	 * @throws InvalidVersionException
	 *             if the message version doesn't match
	 */
	public Message getMessage(int messageTypeIndex, byte[] data) throws IOException {
		/*
		 * Now we check if we have this message class implemented.
		 */
		if (factoryArray.containsKey(messageTypeIndex)) {
			try {
				Message buildMsg = factoryArray.get(messageTypeIndex).newBuilderForType().mergeFrom(data).build();
				return buildMsg;
			}catch (InvalidProtocolBufferException ipbe) {
				logger.error("mergeFrom error type:"+messageTypeIndex, ipbe);
				return null;
			}catch (Exception e) {
				logger.error("error in getMessage", e);
				return null;
			}
		} else {
			logger.warn("Message type [" + messageTypeIndex + "] is not registered in the MessageFactory");
			throw new IOException("Message type [" + messageTypeIndex + "] is not registered in the MessageFactory");
		}
	}

	/**
	 * Returns a object of the right class from a stream of serialized data.
	 *
	 * @param data
	 *            the serialized data
	 * @return a message of the right class
	 * @throws IOException
	 *             in case of problems with the message
	 * @throws InvalidVersionException
	 *             if the message version doesn't match
	 */
	public GeneratedMessageV3 getMessage(int messageTypeIndex, ByteString data) throws IOException {
		/*
		 * Now we check if we have this message class implemented.
		 */
		if (factoryArray.containsKey(messageTypeIndex)) {
			try {
				GeneratedMessageV3 buildMsg = (GeneratedMessageV3)factoryArray.get(messageTypeIndex).newBuilderForType().mergeFrom(data).build();
				return buildMsg;
			}catch (InvalidProtocolBufferException ipbe) {
				logger.error("mergeFrom error type:"+messageTypeIndex, ipbe);
				return null;
			}catch (Exception e) {
				logger.error("error in getMessage", e);
				return null;
			}
		} else {
			logger.warn("Message type [" + messageTypeIndex + "] is not registered in the MessageFactory");
			throw new IOException("Message type [" + messageTypeIndex + "] is not registered in the MessageFactory");
		}
	}
	
	//TODOyyh
	public ProtoMessagePack getMessagePack(int messageTypeIndex, byte[] data) throws IOException {
		BaseMessage.Builder msg = BaseMessage.newBuilder().mergeFrom(data);
		Message retMsg = getMessage(msg.getType(), msg.getData());
		if(null == retMsg) {
			return null;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Recive MsgID:" + messageTypeIndex + " msg:" + retMsg.toString());
		}
		
		return new ProtoMessagePack(msg.build(), retMsg);
	}
	public ProtoMessagePack getMessagePack(byte[] data) throws IOException {
		BaseMessage.Builder msg = BaseMessage.newBuilder().mergeFrom(data);
		Message retMsg = getMessage(msg.getType(), msg.getData());
		if(null == retMsg) {
			throw new IOException("invalid message data");
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Recive MsgID:" + " msg:" + retMsg.toString());
		}
		
		return new ProtoMessagePack(msg.build(), retMsg);
	}
//	public ProtoMessagePack getMessagePack(int messageTypeIndex, byte[] data, int synckey) throws IOException {
//		Message retMsg = getMessage(messageTypeIndex, data);
//		if(null == retMsg) {
//			return null;
//		}
//		
//		if(logger.isDebugEnabled()) {
//			logger.debug("Recive MsgID:" + messageTypeIndex + " msg:" + retMsg.toString());
//		}
//		
//		return new ProtoMessagePack(messageTypeIndex, retMsg);
//	}
	
	public int getMsgIdByMessageClass(Class<?> msgClass) {
		if(revertFactoryArray.containsKey(msgClass)) {
			try {
				Integer retVal = revertFactoryArray.get(msgClass);
				if(null == retVal) {
					return -1;
				}
				
				return retVal.intValue();
			} catch(Exception e) {
				logger.error("MessageFactory getMsgIdByMessageClass is null", e);
				return -1;
			}
		}
		
		return -1;
	}
	
	/**
	 * 是否是同步包
	 * @param msg
	 * @return
	 */
	public boolean isSyncMsg(GeneratedMessage msg) {
		int nMsgId = getMsgIdByMessageClass(msg.getClass());
		if(-1 == nMsgId) {
			return false;
		}
		
		return true;
		//return Messagetype.MessageType.MSGTYPE_SYNC_BENCHMARK_VALUE >= nMsgId;
	}
	
}
