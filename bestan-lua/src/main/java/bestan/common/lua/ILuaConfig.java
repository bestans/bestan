package bestan.common.lua;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import bestan.log.Glog;

/**
 * 提供一个接口，读取lua配置
 * 支持Integer/int/Boolean/boolean/Short/short/
 * String/Long/long/ILuaConfig/List<T>/map<T1, T2> 等类型
 * <br>
 * 每一种配置，可以通过LuaAnnotation指定配置项信息，比如配置文件名等
 * <br>
 * 如果没有指定配置文件名，那么采用classname.lua组合作为配置文件名
 * 
 * @author yeyouhuan
 * @date:   2018年8月2日 下午7:50:27 
 */
public interface ILuaConfig {
	private Integer parseInt(LuaValue luaValue) {
		if (!luaValue.isnumber()) {
			throw new LuaException("unexpected type:" + luaValue.typename());
		}
		return luaValue.toint();
	}
	private Boolean parseBool(LuaValue luaValue) {
		if (!luaValue.isnumber()) {
			throw new LuaException("unexpected type:" + luaValue.typename());
		}
		return luaValue.toboolean();
	}
	private Short parseShort(LuaValue luaValue) {
		if (!luaValue.isnumber()) {
			throw new LuaException("unexpected type:" + luaValue.typename());
		}
		return luaValue.toshort();
	}
	private String parseString(LuaValue luaValue) {
		if (!luaValue.isstring()) {
			throw new LuaException("unexpected type:" + luaValue.typename());
		}
		return luaValue.toString();
	}
	private Long parseLong(LuaValue luaValue) {
		if (!luaValue.isstring()) {
			throw new LuaException("unexpected type:" + luaValue.typename());
		}
		String value = luaValue.toString();
		return Long.valueOf(value);
	}
	private void parseLuaConfig(LuaValue luaValue, ILuaConfig config) {
		if (!luaValue.istable()) {
			throw new LuaException("unexpected type:" + luaValue.typename());
		}
		var fields = config.getClass().getFields();
		var luaAnnotation = config.getClass().getAnnotation(LuaAnnotation.class);
		for (var it : fields) {
			try {
				LuaValue tempLuaValue = luaValue.get(LuaString.valueOf(it.getName()));
				if (tempLuaValue == LuaValue.NIL) {
					var annotation = it.getAnnotation(LuaParamAnnotation.class);
					if ((luaAnnotation != null && luaAnnotation.optional())
						|| (annotation != null && annotation.optional())) {
						//配置项可选，跳过此项配置
						continue;
					}

					throw new LuaException("missing config");
				}
				var tempValue = parseLuaValue(tempLuaValue, null, it.getType(), it.getGenericType());
				it.set(config, tempValue);
			} catch (Exception e) {
				StringBuffer stringBuffer =new StringBuffer();
				stringBuffer.append("[section=").append(it.getName()).append(",error=").append(e.getMessage()).append("]");
				throw new LuaException(stringBuffer.toString());
			}
		}
	}
	private <T> void parseList(LuaValue luaValue, List<T> arr, Class<T> tClass, Type tType) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		if (!luaValue.istable()) {
			throw new LuaException("unexpected type:" + luaValue.typename());
		}
		
		var luaTable = (LuaTable)luaValue;
		for (var n = luaTable.next(LuaValue.NIL); !n.arg1().isnil(); n = luaTable.next(n.arg1())) {
			LuaValue value = n.arg(2);
			arr.add(parseLuaValue(value, null, tClass, tType));
		}
	}
	private <T1, T2> void parseMap(LuaValue luaValue, Map<T1, T2> map, Class<T1> t1Class, Type t1Type, Class<T2> t2Class, Type t2Type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (!luaValue.istable()) {
			throw new LuaException("unexpected type:" + luaValue.typename());
		}
		
		var luaTable = (LuaTable)luaValue;
		for (var n = luaTable.next(LuaValue.NIL); !n.arg1().isnil(); n = luaTable.next(n.arg1())) {
			LuaValue key = n.arg1();
			LuaValue value = n.arg(2);
			var t1 = parseLuaValue(key, null, t1Class, t1Type);
			var t2 = parseLuaValue(value, null, t2Class, t2Type);
			map.put(t1, t2);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T parseLuaValue(LuaValue luaValue, T value, Class<? extends T> cls,  Type type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (ILuaConfig.class.isAssignableFrom(cls)) {
			if (value == null) {
				value = cls.getDeclaredConstructor().newInstance();
			}
			parseLuaConfig(luaValue, (ILuaConfig)value);
		} else if (cls.equals(Integer.class) || cls.equals(int.class)) {
			value = (T) parseInt(luaValue);
		} else if (cls.equals(Boolean.class) || cls.equals(boolean.class)) {
			value = (T) parseBool(luaValue);
		} else if (cls.equals(Short.class) || cls.equals(short.class)) {
			value = (T) parseShort(luaValue);
		} else if (cls.equals(String.class)) {
			value = (T) parseString(luaValue);
		} else if (cls.equals(Long.class) || cls.equals(long.class)) {
			value = (T) parseLong(luaValue);
		} else if (cls.equals(List.class)) {
			if (value == null) {
				value = (T) new ArrayList<>();
			}
			var pType = (ParameterizedType) type;
	        var dataType = pType.getActualTypeArguments()[0];
	        var dataClass = (dataType instanceof ParameterizedType) ? (Class<?>)((ParameterizedType)dataType).getRawType() : (Class<?>)dataType;
			parseList(luaValue, (List)value, dataClass, dataType);
		} else if (cls.equals(Map.class)) {
			if (value == null) {
				value = (T)new HashMap<>();
			}
			var pType = (ParameterizedType) type;
			var args = pType.getActualTypeArguments();
	        var keyClass = (args[0] instanceof ParameterizedType) ? (Class<?>)((ParameterizedType)args[0]).getRawType() : (Class<?>)args[0];
	        var valueClass = (args[1] instanceof ParameterizedType) ? (Class<?>)((ParameterizedType)args[1]).getRawType() : (Class<?>)args[1];
			parseMap(luaValue, (Map)value, keyClass, args[0], valueClass, args[1]);
		} else
		{	
			throw new LuaException("unsurported type " + cls.getSimpleName());
		}
		return value;
	}
	
	default boolean LoadLuaConfig(Globals globals, String path) {
		try {
			LuaValue chunk = globals.loadfile(path);
			var ret = chunk.call();
			parseLuaConfig(ret, this);
			afterLoad();
		} catch (Exception e) {
			Glog.trace("LoadLuaConfig {} failed:message={}", path, e.getMessage());
			return false;
		}
		return true;
	}

	default boolean LoadLuaConfig(String path) {
		Globals globals = JsePlatform.standardGlobals();
		return LoadLuaConfig(globals, path);
	}
	
	default void LoadLuaConfig() {
		var luaAnnotation = getClass().getAnnotation(LuaAnnotation.class);
		String path = luaAnnotation != null ? luaAnnotation.fileName() : getClass().getSimpleName() + ".lua";
		LoadLuaConfig(path);
	}
	
	default void afterLoad() {}
}
