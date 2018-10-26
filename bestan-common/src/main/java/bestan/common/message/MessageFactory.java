package bestan.common.message;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.Message;

import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.module.IModule;
import bestan.common.module.StartupException;
import bestan.common.net.handler.IMessageHandler;
import bestan.common.net.handler.IRpcClientHandler;
import bestan.common.net.handler.IRpcServerHandler;
import bestan.common.net.handler.NoteMessageHandler;
import bestan.common.protobuf.MessageFixedEnum;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * message索引是生成的，按照messageName编号<p>
 * message、messageHandle相关都是根据所在包名通过反射建立的<p>
 * 新增的message、messageHandle只要在指定包里，就自动载入到factory<p>
 * 另外messageHandle必须按照xxxhandler命名规则进行命名
 * 
 * @author yeyouhuan
 *
 */
public class MessageFactory {
	private static Map<Class<? extends Message>, Integer> messageIndexMap = Maps.newHashMap();
	private static Map<Class<? extends Message>, Message> messageInstanceMap = Maps.newHashMap();
	private static Map<Integer, Message> indexMessageMap = Maps.newHashMap();
	private static Map<String, Integer> messageNameIndexMap = Maps.newHashMap();
	private static Map<String, Message> nameMessageMap = Maps.newHashMap();
	
	private static List<IMessageLoadFinishCallback> loadFinishCallbackList = Lists.newArrayList();
	/**
	 * 消息对应的handler
	 */
	private static Map<Integer, IMessageHandler> messageHandleMap = Maps.newHashMap();
	/**
	 * rpc消息对应的clientHandle
	 */
	private static Map<Integer, IRpcClientHandler> rpcClientHandleMap = Maps.newHashMap();
	/**
	 * rpc消息对应的serverHandle
	 */
	private static Map<Integer, IRpcServerHandler> rpcserverHandleMap = Maps.newHashMap();
	
	private static boolean register(Class<? extends Message> messageClass) {
		return register(messageClass, messageNameIndexMap.get(messageClass.getSimpleName()));
	}
		
	private static boolean register(Class<? extends Message> messageClass, Integer newIndex) {
		if (newIndex == null) {
			return false;
		}
		String messageName = messageClass.getSimpleName();
		if (indexMessageMap.containsKey(newIndex) || messageIndexMap.containsKey(messageClass) || nameMessageMap.containsKey(messageName)) {
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
			nameMessageMap.put(messageName, (Message)message);
		} catch (Exception e) {
			Glog.error("MessageFactory register error={}, message={}", e, messageClass.getSimpleName());
			return false;
		}
		return true;
	}
	
	private static boolean registerMessagehandler(Class<? extends IMessageHandler> cls) {
		var note = cls.getAnnotation(NoteMessageHandler.class);
		if (note != null && note.discard()) {
			//废弃的handle
			return true;
		}
		String messageName = null;
		if (note != null) {
			//采用制定messageName
			messageName = note.messageName();
		}
		var handleName = cls.getSimpleName();
		if (Strings.isNullOrEmpty(messageName)) {
			//根据handler名解析出messageName
			if (handleName.length() <= 7) {
				Glog.error("loadMessageHandle erorr:invalid handle name({}), valid e.g xxxxHandle", handleName);
				return false;
			}
			messageName = handleName.substring(0, handleName.length() - 7);
		}
		var messageIndex = messageNameIndexMap.get(messageName);
		if (messageIndex == null) {
			Glog.error("registerMessagehandler:handler={},messageName={},messageNameIndexMap={}", cls.getSimpleName(), messageName, messageNameIndexMap);
			return false;
		}
		return registerMessagehandler(cls, messageIndex);
	}
	private static boolean registerMessagehandler(Class<? extends IMessageHandler> cls, Integer messageIndex) {
		var handleName = cls.getSimpleName();
		if (messageIndex == null) {
			Glog.error("loadMessageHandle erorr:can not find messageindex, handle=({}), valid e.g xxxxHandle", handleName);
			return false;
		}
		try {
			Glog.debug("loadMessageHandle:{}", cls.getSimpleName());
			var handle = cls.getDeclaredConstructor().newInstance();
			if (IRpcClientHandler.class.isAssignableFrom(cls)) {
				if (rpcClientHandleMap.get(messageIndex) != null) {
					Glog.error("loadMessageHandle error: duplicate rpcClientHandle:messageIndex={}, oldHandler={}, newHandler={}",
							messageIndex, rpcClientHandleMap.get(messageIndex).getClass().getSimpleName(), handleName);
					return false;
				}
				rpcClientHandleMap.put(messageIndex, (IRpcClientHandler)handle);
			} else if (IRpcServerHandler.class.isAssignableFrom(cls)) {
				if (rpcserverHandleMap.get(messageIndex) != null) {
					Glog.error("loadMessageHandle error: duplicate rpcserverHandle:messageIndex={}, oldHandler={}, newHandler={}",
							messageIndex, rpcserverHandleMap.get(messageIndex).getClass().getSimpleName(), handleName);
					return false;
				}
				rpcserverHandleMap.put(messageIndex, (IRpcServerHandler)handle);
			} else {
				if (messageHandleMap.get(messageIndex) != null) {
					Glog.error("loadMessageHandle error: duplicate messageHandle:messageIndex={}, oldHandler={}, newHandler={}",
							messageIndex, messageHandleMap.get(messageIndex).getClass().getSimpleName(), handleName);
					return false;
				}
				messageHandleMap.put(messageIndex, handle);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Glog.error("loadMessageHandle newInstance error: handle name({}), valid e.g xxxxHandle", handleName);
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
	
	public static Message getMessageInstance(String messageName) {
		return nameMessageMap.get(messageName);
	}
	
	public static int getMessageIndex(Class<? extends Message> messageClass) {
		var messageIndex = messageIndexMap.get(messageClass);
		return messageIndex != null ? messageIndex : 0;
	}
	
	@SuppressWarnings("unchecked")
	public static int getMessageIndex(Message.Builder messageBuilder) {
		return getMessageIndex((Class<? extends Message>)messageBuilder.getClass().getEnclosingClass());
	}
	
	public static int getMessageIndex(Message message) {
		if (message == null) return 0;
		
		return getMessageIndex(message.getClass());
	}
	
	public static IMessageHandler getMessageHandle(int messageIndex) {
		return messageHandleMap.get(messageIndex);
	}
	
	public static IRpcClientHandler getRpcClientHandler(int messageIndex) {
		return rpcClientHandleMap.get(messageIndex);
	}
	
	public static IRpcServerHandler getRpcServerHandler(int messageIndex) {
		return rpcserverHandleMap.get(messageIndex);
	}
	
	private static boolean isSubInterface(Class<?> cls, String superName) {
		if (cls.equals(Object.class)) {
			return false;
		}
		for (var it : cls.getGenericInterfaces()) {
			if (it.getTypeName().equals(superName)) {
				return true;
			}
			try {
				if (isSubInterface(Class.forName(it.getTypeName()), superName)) {
					return true;
				}
			} catch (ClassNotFoundException e) {
				return false;
			}
		}
		return false;
	}
	
	public static List<Class<?>> loadClasses(String packageName) {
		ScanResult scanResult =
		        new ClassGraph()
		            .enableAllInfo()
		            .whitelistPackages(packageName)
		            .scan();
		var allCls = scanResult.getAllClasses();
	    ClassInfoList filtered = allCls
        .filter(classInfo -> {
        	return !(classInfo.isInterface() || classInfo.isAbstract());
        });
		return filtered.loadClasses();
	}

	public static List<Class<?>> loadClasses(String packageName, String interfaceName) {
		ScanResult scanResult =
		        new ClassGraph()
		            .enableAllInfo()
		            .whitelistPackages(packageName)
		            .scan();
		var allCls = scanResult.getAllClasses();
	    ClassInfoList filtered = allCls
        .filter(classInfo -> {
        	return !(classInfo.isInterface() || classInfo.isAbstract())
        			&& isSubInterface(classInfo.loadClass(), interfaceName);
        });
		return filtered.loadClasses();
	}

	public static List<Class<?>> loadClasses(String packageName, Class<?> baseClass) {
		ScanResult scanResult =
		        new ClassGraph()
		            .enableAllInfo()
		            .whitelistPackages(packageName)
		            .scan();
		var allCls = scanResult.getAllClasses();
	    ClassInfoList filtered = allCls
        .filter(classInfo -> {
        	return !(classInfo.isInterface() || classInfo.isAbstract())
        			&& baseClass.isAssignableFrom(classInfo.loadClass());
        });
		return filtered.loadClasses();
	}
	
	/**
	 * 载入message handle类，message handle命名必须为xxxxHandle
	 * @param packageName
	 */
	@SuppressWarnings("unchecked")
	public static boolean loadMessageHandle(String packageName) {
		ScanResult scanResult =
		        new ClassGraph()
		            .enableAllInfo()
		            .whitelistPackages(packageName)
		            .scan();
		var allCls = scanResult.getAllClasses();
	    ClassInfoList filtered = allCls
        .filter(classInfo -> {
        	return !(classInfo.isInterface() || classInfo.isAbstract())
        			&& isSubInterface(classInfo.loadClass(), "bestan.common.net.handler.IMessageHandler");
        });
	    var classes = filtered.loadClasses();
	    Glog.debug("loadMessageHandle:packageName={},size={}", packageName, classes.size());
		for (var cls : classes) {
			if (Modifier.isAbstract(cls.getModifiers())) {
				//抽象类
				continue;
			}
			if (!registerMessagehandler((Class<? extends IMessageHandler>) cls)) {
				return false;
			}
		}
		return true;
	}

	public static boolean loadMessage(String packageName) {
		Reflections.log = null;
		Reflections reflections = new Reflections(packageName);
		Set<Class<? extends Message>> classes = reflections.getSubTypesOf(Message.class);
		Glog.debug("loadmessage:size={}", classes.size());
		for (var cls : classes) {
			if (Modifier.isAbstract(cls.getModifiers())) {
				//抽象类
				continue;
			}
			if (cls.getEnclosingClass() != null && cls.getEnclosingClass().isMemberClass()) {
				//表示是message里嵌套的message
				continue;
			}
			Glog.debug("loadMessage:{}", cls.getSimpleName());
			if (!register(cls)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static void loadMessageIndex(Class<? extends Enum> cls) {
		for (var it : cls.getEnumConstants()) {
			messageNameIndexMap.put(it.name(), it.ordinal());
		}
	}
	
	private static boolean loadFixedMessage() {
		for (var it : MessageFixedEnum.values()) {
			if (it.ordinal() == 0) {
				//跳过无效的枚举
				continue;
			}
			var cls = it.getMessageClass();
			messageNameIndexMap.put(cls.getSimpleName(), it.getMessageId());
			if (!register(cls, it.getMessageId())) {
				return false;
			}
			var handlerCls = it.getMessageHandleClass();
			if (handlerCls != null && !registerMessagehandler(handlerCls, it.getMessageId())) {
				return false;
			}
		}
		return true;
	}
	
	public static void registerLoadFinishCallback(IMessageLoadFinishCallback object) {
		loadFinishCallbackList.add(object);
	}
	
	//执行message载入回调操作
	public static void executeLoadFinishCallback() {
		//执行回调
		for (var obj : loadFinishCallbackList) {
			try {
				obj.onMessageLoadFinish();
			} catch (Exception e) {
				throw new FormatException("loadFinishCallback failed:" + e.getMessage());
			}
		}
		loadFinishCallbackList.clear();
	}
	
	public static class MessageModule implements IModule {
		@SuppressWarnings("rawtypes")
		private Class<? extends Enum> messageIndex;
		private List<String> messagePackages;
		private List<String> messageHandlerPackages;
		
		@SuppressWarnings("rawtypes")
		public MessageModule(Class<? extends Enum> messageIndex, List<String> messagePackages, List<String> messageHandlerPackages) {
			this.messageIndex = messageIndex;
			this.messagePackages = messagePackages != null ? messagePackages : Lists.newArrayList();
			this.messageHandlerPackages = messageHandlerPackages != null ? messageHandlerPackages : Lists.newArrayList();
		}

		@SuppressWarnings("rawtypes")
		public MessageModule(Class<? extends Enum> messageIndex, String[] messagePackages, String[] messageHandlerPackages) {
			this.messageIndex = messageIndex;
			this.messagePackages = messagePackages != null ? Lists.newArrayList(messagePackages) : Lists.newArrayList();
			this.messageHandlerPackages = messageHandlerPackages != null ? Lists.newArrayList(messageHandlerPackages) : Lists.newArrayList();
		}
		
		@Override
		public void startup() {
			if (!loadFixedMessage()) {
				throw new StartupException(this, "loadFixedMessage failed");
			}
			//载入messageName->index映射关系
			if (messageIndex != null) {
				loadMessageIndex(messageIndex);
			}
			//载入message
			for (var it : messagePackages) {
				if (!loadMessage(it)) {
					throw new StartupException(this, "loadMessage failed:messagePackage=" + it);
				}
			}
			//载入messsageHandler
			for (var it : messageHandlerPackages) {
				if (!loadMessageHandle(it)) {
					throw new StartupException(this, "loadMessageHandle failed:messageHandlerPackage=" + it);
				}
			}
			
			executeLoadFinishCallback();
		}
	}
}
