package bestan.lua;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.lang.reflect.Type;

import bestan.log.Glog;

public interface ILuaConfig {
	default Integer parseInt(LuaValue luaValue) {
		System.out.println("parseInt=" + luaValue.toint());
		return luaValue.toint();
	}
	default void parseBool(LuaValue luaValue, Boolean t) {
		t = luaValue.toboolean();
	}
	default void parseShort(LuaValue luaValue, Short t) {
		t = luaValue.toshort();
	}
	default void parseLuaConfig(LuaValue luaValue, ILuaConfig config) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		var fields = config.getClass().getFields();
		for (var it : fields) {
			LuaValue tempLuaValue = luaValue.get(LuaString.valueOf(it.getName()));
			var value = it.get(config);
			Field type = null;
			try {
				type = config.getClass().getDeclaredField(it.getName());
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Glog.trace("value_type={}{}",it.getType(), it.getGenericType());
			if (it.getType().getInterfaces().length > 0) {
				Glog.trace("value_type111={}", it.getType().getInterfaces()[0]);
			}
			var tempValue = parseLuaValue(tempLuaValue, null, it.getType(), it.getGenericType());
			it.set(config, tempValue);
		}
	}
	default <T> void parseList(LuaValue luaValue, List<T> arr, Class<T> tClass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (!luaValue.istable()) return;
		
		var luaTable = (LuaTable)luaValue;
		for (var n = luaTable.next(LuaValue.NIL); !n.arg1().isnil(); n = luaTable.next(n.arg1())) {
			LuaValue key = n.arg1();
			LuaValue value = n.arg(2);
			
			Glog.trace("aaa={}{}", tClass.getName(), tClass.getGenericSuperclass());
			T t = null;
			t = parseLuaValue(value, t, tClass, tClass.getGenericSuperclass());
			arr.add(t);
		}
	}
	default <T1, T2> void parseMap(LuaValue luaValue, Map<T1, T2> map, Class<T1> t1Class, Class<T2> t2Class) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (!luaValue.istable()) return;
		
		var luaTable = (LuaTable)luaValue;
		for (var n = luaTable.next(LuaValue.NIL); !n.arg1().isnil(); n = luaTable.next(n.arg1())) {
			LuaValue key = n.arg1();
			LuaValue value = n.arg(2);
			
			Glog.trace("parseMap={},{},{},{}", t1Class.getName(), t1Class.getGenericSuperclass(),
					t2Class.getName(), t2Class.getGenericSuperclass());
			T1 t1 = null;
			t1 = parseLuaValue(key, t1, t1Class, t1Class.getGenericSuperclass());
			T2 t2 = null;
			t2 = parseLuaValue(value, t2, t2Class, t2Class.getGenericSuperclass());
			map.put(t1, t2);
		}
	}

	default <T> T parseLuaValue(LuaValue luaValue, T value, Class<? extends T> cls,  Type type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		var interfaces = cls.getInterfaces();
		Glog.trace("parseLuaValue1={}{}", cls.getName(), interfaces.length);
		if (interfaces.length > 0 && interfaces[0].equals(ILuaConfig.class)) {
			if (value == null) {
				value = cls.getDeclaredConstructor().newInstance();
			}
			parseLuaConfig(luaValue, (ILuaConfig)value);
		} else if (cls.equals(Integer.class)) {
			value = (T) parseInt(luaValue);
			System.out.println("parseInt22=" + value);
		} else if (cls.equals(Boolean.class)) {
			if (value == null) {
				value = (T) Boolean.valueOf(false);
			}
			parseBool(luaValue, (Boolean)value);
		} else if (cls.equals(Short.class)) {
			if (value == null) {
				value = (T) Short.valueOf((short) 0);
			}
			parseShort(luaValue, (Short)value);
		} else if (cls.equals(String.class)) {
			return (T) luaValue.toString();
		} else if (interfaces.length > 0 && interfaces[0].equals(Collection.class)) {
			if (value == null) {
				Glog.trace("clsclsclscls={}", cls.getName());
				value = (T) new ArrayList();
			}
			ParameterizedType stringListType = (ParameterizedType) type;
	        Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
	        System.out.println(stringListClass.getName()+stringListType.getActualTypeArguments().length); // class java.lang.String.
			parseList(luaValue, (List)value, stringListClass);
		} else if (cls.equals(Map.class)) {
			if (value == null) {
				value = (T)new HashMap<>();
			}
			ParameterizedType stringListType = (ParameterizedType) type;
	        Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
	        Class<?> stringListClass1 = (Class<?>) stringListType.getActualTypeArguments()[1];
			parseMap(luaValue, (Map)value, stringListClass, stringListClass1);
		} else
		{	
			throw new LuaError("parse config failed, config=" + cls.getSimpleName() + ",super=" + cls.getSuperclass().getName());
		}
		return value;
	}
	
	default void LoadLuaConfig(String path) {
		Globals globals = JsePlatform.standardGlobals();
		LuaValue chunk = globals.loadfile(path);
		var ret = chunk.call();
		try {
			Glog.trace("LoadLuaConfig={}", getClass().getSimpleName());
			parseLuaValue(ret, this, getClass(), getClass().getGenericSuperclass());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	default void LoadLuaConfig() {
		
	}
}
