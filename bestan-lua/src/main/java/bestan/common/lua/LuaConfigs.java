package bestan.common.lua;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.reflections.Reflections;

import com.google.common.base.Strings;

import bestan.log.Glog;

/**
 * lua配置管理器，启动时调用loadConfig载入所有lua配置，
 * 
 * @author yeyouhuan
 * @date:   2018年8月2日 下午7:53:04 
 */
public class LuaConfigs {
	private Map<Class<?>, ILuaConfig> allConfigs = new HashMap<>();
	private static LuaConfigs instance = new LuaConfigs();
	private static Globals globals = JsePlatform.standardGlobals();
	
	/**
	 * 获取lua配置
	 * 
	 * @param cls lua配置class
	 * @return lua配置
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ILuaConfig> T get(Class<T> cls) {
		return (T) instance.allConfigs.get(cls);
	}
	
	/**
	 * 载入所有lua配置
	 * 
	 * @param rootPath lua配置根目录
	 * @param packageNames lua配置java文件所在包列表
	 * @return 成功或失败
	 */
	public static boolean loadConfig(String rootPath, String[] packageNames) {
		try {
			for (var pName : packageNames) {
				Reflections.log = null;
				Reflections reflections = new Reflections(pName);
				Set<Class<? extends ILuaConfig>> classes = reflections.getSubTypesOf(ILuaConfig.class);
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
	 * 载入lua配置
	 * 
	 * @param rootPath lua配置根目录
	 * @param cls lua配置class
	 * @return 成功或失败
	 */
	public static boolean loadConfig(String rootPath, Class<? extends ILuaConfig> cls) {
		try {
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
				ILuaConfig config = (ILuaConfig)cls.getDeclaredConstructor().newInstance();
				if (!config.LoadLuaConfig(globals, rootPath + fileName)) {
					return false;
				}
				instance.allConfigs.put(cls, config);
			}
		} catch (Exception e) {
			Glog.trace("LuaConfigs init failed.error={}", e.getMessage());
		}
		return true;
	}
	
	/**
	 * 载入lua配置
	 * 
	 * @param cls lua配置class
	 * @return 成功或失败
	 */
	public static boolean loadConfig(Class<? extends ILuaConfig> cls) {
		return loadConfig("", cls);
	}
}
