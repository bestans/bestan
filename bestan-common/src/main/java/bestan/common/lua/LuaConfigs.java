package bestan.common.lua;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.reflections.Reflections;

import com.google.common.base.Strings;

import bestan.common.log.Glog;
import bestan.common.logic.FormatException;
import bestan.common.message.MessageFactory;

/**
 * lua配置管理器，启动时调用loadConfig载入所有lua配置，
 * 
 * @author yeyouhuan
 * @date:   2018年8月2日 下午7:53:04 
 */
public class LuaConfigs {
	private static LuaConfigs instance = new LuaConfigs();
	private static Globals globals = JsePlatform.standardGlobals();
	
	private Map<Class<?>, BaseLuaConfig> allConfigs = new HashMap<>();
	
	/**
	 * 获取lua配置
	 * 
	 * @param cls lua配置class
	 * @return lua配置
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseLuaConfig> T get(Class<T> cls) {
		return (T) instance.allConfigs.get(cls);
	}
	
	/**
	 * 载入所有lua配置
	 * 
	 * @param rootPath lua配置根目录
	 * @param packageNames lua配置java文件所在包列表
	 * @return 成功或失败
	 */
	public static boolean loadConfigOld(String rootPath, String[] packageNames) {
		try {
			for (var pName : packageNames) {
				Glog.debug("loadConfig rootPath={},package={}", rootPath, pName);
				Reflections.log = null;
				Reflections reflections = new Reflections(pName);
				Set<Class<? extends BaseLuaConfig>> classes = reflections.getSubTypesOf(BaseLuaConfig.class);
				Glog.trace("loadConfig={}", classes.size());
				for (var cls : classes) {
					if (cls.isMemberClass()) {
						//嵌套类 不处理
						continue;
					}
					if (!loadConfig(rootPath, cls))
						return false;
				}
			}
		} catch (Exception e) {
			Glog.trace("LuaConfigs init failed.error={}", e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * 载入所有lua配置
	 * 
	 * @param rootPath lua配置根目录
	 * @param packageNames lua配置java文件所在包列表
	 * @return 成功或失败
	 */
	@SuppressWarnings("unchecked")
	public static boolean loadConfig(String rootPath, String[] packageNames) {
		try {
			for (var pName : packageNames) {
				Glog.debug("loadConfig rootPath={},package={}", rootPath, pName);
				var classes = MessageFactory.loadClasses(pName, BaseLuaConfig.class);
				Glog.trace("loadConfig={}", classes.size());
				for (var cls : classes) {
					if (cls.isMemberClass()) {
						//嵌套类 不处理
						continue;
					}
					if (!loadConfig(rootPath, (Class<? extends BaseLuaConfig>)cls))
						return false;
				}
			}
		} catch (Exception e) {
			Glog.trace("LuaConfigs init failed.error={}", e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * 载入所有lua配置
	 * 
	 * @param packageNames lua配置java文件所在包列表
	 * @return 成功或失败
	 */
	public static boolean loadConfig(String[] packageNames) {
		return loadConfig("", packageNames);
	}
	
	/**
	 * 载入所有lua配置
	 * 
	 * @param packageName lua配置java文件所在包
	 * @return 成功或失败
	 */
	public static boolean loadConfig(String packageName) {
		String[] arr = { packageName };
		return loadConfig(arr);
	}
	
	/**
	 * 载入所有lua配置
	 * 
	 * @param rootPath lua配置根目录
	 * @param packageName lua配置java文件所在包
	 * @return 成功或失败
	 */
	public static boolean loadConfig(String rootPath, String packageName) {
		String[] arr = { packageName };
		return loadConfig(rootPath, arr);
	}
	
	/**
	 * @param rootPath 配置所在文件夹
	 * @param cls 配置class
	 * @return
	 */
	public static <T extends BaseLuaConfig> T loadLuaConfig(String rootPath, Class<T> cls) {
		var annotation = cls.getAnnotation(LuaAnnotation.class);
		String fileName = null;
		if (annotation != null) {
			fileName = annotation.fileName();
		}
		if (Strings.isNullOrEmpty(fileName)) {
			//如果没有指定配置文件名，那么使用classname和.lua组合
			fileName = cls.getSimpleName() + ".lua";
		}
		if (annotation == null || annotation.load()) {
			return loadSingleConfig(rootPath + fileName, cls);
		}
		return null;
	}
	
	/**
	 * @param fullPath 配置的完整路径
	 * @param cls 配置的class
	 * @return 指定BaseLuaConfig配置
	 */
	public static <T extends BaseLuaConfig> T loadSingleConfig(String fullPath, Class<T> cls) {
		T config = null;
		try {
			config = (T)cls.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(String.format("loadSingleConfig exception:class=%s,info=%s", e.getClass().getSimpleName(), e.getMessage()));
		}
		if (!config.LoadLuaConfig(globals, fullPath)) {
			return null;
		}
		return config;
	}
	
	/**
	 * 载入lua配置
	 * 
	 * @param rootPath lua配置根目录
	 * @param cls lua配置class
	 * @return 成功或失败
	 */
	public static boolean loadConfig(String rootPath, Class<? extends BaseLuaConfig> cls) {
		try {
			Glog.debug("loadConfig rootPath={},class={}", rootPath, cls);
			var config = loadLuaConfig(rootPath, cls);
			if (null == config) return true;
			
			//设置配置单例
			try
			{
				var configInstance = cls.getField("instance");
				if (configInstance.get(null) != null) {
					throw new FormatException("instance has value, maybe have more than one config:{}", cls.getSimpleName());
				}
				if (configInstance != null) {
					configInstance.set(null, config);	
				}
			} catch (NoSuchFieldException e) {
				
			}
			instance.allConfigs.put(cls, config);
			return true;
		} catch (Exception e) {
			Glog.trace("LuaConfigs init failed.error={},{}", e.getClass().getSimpleName(), e.getMessage());
		}
		return false;
	}
	
	/**
	 * 载入lua配置
	 * 
	 * @param cls lua配置class
	 * @return 成功或失败
	 */
	public static boolean loadConfig(Class<? extends BaseLuaConfig> cls) {
		return loadConfig("", cls);
	}
}
