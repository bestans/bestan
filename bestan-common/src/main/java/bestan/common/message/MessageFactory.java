package bestan.common.message;

import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.logic.ServerConfig;
import bestan.common.module.IModule;
import bestan.common.module.StartupException;

/**
 * message索引是根据class name计算而得，<p>
 * 先将class name转换位小写，然后a,b,c,d.....分别表示0,1,2,3....<p>
 * 按照26进制计算而得，例如"abC" 计算结果 0 * 26 * 26 + 1 * 26 + 2 = 28 <p> 
 * 
 * message、messageHandle相关都是根据所在包名通过反射建立的<p>
 * 新增的message、messageHandle只要在指定包里，就自动载入到factory<p>
 * 另外messageHandle必须按照xxxHandle命名规则进行命名
 * 
 * @author yeyouhuan
 *
 */
public class MessageFactory implements IModule {
	private static Map<Class<? extends Message>, Integer> messageIndexMap;
	private static Map<Class<? extends Message>, Message> messageInstanceMap;
	private static Map<Integer, Message> indexMessageMap;
	private static Map<String, Integer> messageNameIndexMap;
	private static Map<Integer, IMessageHandle> messageHandleMap;
	
	private static boolean register(Class<? extends Message> messageClass) {
		int newIndex = makeMessageIndex(messageClass);
		if (indexMessageMap.containsKey(newIndex) || messageIndexMap.containsKey(messageClass)) {
			Glog.error("MessageFactory register error index, class={}, index{}, duplicate with {}",
					messageClass.getSimpleName(), newIndex,
					getMessageInstance(newIndex).getClass().getSimpleName());  
			return false;
		}
		try {
			var method = messageClass.getMethod("getDefaultInstance");
			var message = method.invoke(null);
			if (!(message instanceof Message)) return false;
			
			indexMessageMap.put(newIndex, (Message) message);
			messageIndexMap.put(messageClass, newIndex);
			messageInstanceMap.put(messageClass, (Message)message);
			messageNameIndexMap.put(messageClass.getSimpleName(), newIndex);
		} catch (Exception e) {
			Glog.error("MessageFactory register error={}, message={}", e.getMessage(), messageClass.getSimpleName());
			return false;
		}
		return true;
	}
	
	public static Message getMessageInstance(int index) {
		return indexMessageMap.get(index);
	}
	
	public static Message getMessageInstance(Class<? extends Message> messageClass) {
		return messageInstanceMap.get(messageClass);
	}
	
	public static int getMessageIndex(Class<? extends Message> messageClass) {
		var messageIndex = messageIndexMap.get(messageClass);
		return messageIndex != null ? messageIndex : -1;
	}
	
	public static IMessageHandle getMessageHandle(int messageIndex) {
		return messageHandleMap.get(messageIndex);
	}
	
	/**
	 * 根据message的class name计算出索引
	 * @param className
	 * @return
	 */
	public static int makeMessageIndex(Class<?> className) {
		int value = 0;
		var name = className.getSimpleName().toLowerCase();
		for (int i = 0; i < name.length(); ++i) {
			value += (name.charAt(i) - Character.valueOf('a')) * (int)Math.pow(26, i);
		}
		return value;
	}

	/**
	 * 载入message handle类，message handle命名必须为xxxxHandle
	 * @param packageName
	 */
	private static boolean loadMessageHandle(String packageName) {
		Reflections.log = null;
		Reflections reflections = new Reflections(packageName);
		Set<Class<? extends IMessageHandle>> classes = reflections.getSubTypesOf(IMessageHandle.class);
		for (var cls : classes) {
			if (cls.isMemberClass()) {
				//嵌套类 不处理
				continue;
			}
			var handleName = cls.getSimpleName();
			if (handleName.length() <= 6) {
				Glog.error("loadMessageHandle erorr:invalid handle name({}), valid e.g xxxxHandle", handleName);
				return false;
			}
			var messageName = handleName.substring(0, handleName.length() - 6);
			var messageIndex = messageNameIndexMap.get(messageName);
			if (messageIndex == null) {
				Glog.error("loadMessageHandle erorr:can not find messageindex, handle=({}), valid e.g xxxxHandle", handleName);
				return false;
			}
			try {
				var handle = cls.getDeclaredConstructor().newInstance();
				messageHandleMap.put(messageIndex, handle);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Glog.error("loadMessageHandle newInstance erorr: handle name({}), valid e.g xxxxHandle", handleName);
				return false;
			}
		}
		return true;
	}

	private static boolean loadMessage(String packageName) {
		Reflections.log = null;
		Reflections reflections = new Reflections(packageName);
		Set<Class<? extends Message>> classes = reflections.getSubTypesOf(Message.class);
		for (var cls : classes) {
			if (cls.isMemberClass()) {
				//嵌套类 不处理
				continue;
			}
			
			if (!register(cls)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void startup(ServerConfig config) {
		if (!loadMessage(config.messagePackage)) {
			throw new StartupException(this, "loadMessage failed");
		}
		if (!loadMessageHandle(config.messageHandlerPackage)) {
			throw new StartupException(this, "loadMessageHandle failed");
		}
	}
}
