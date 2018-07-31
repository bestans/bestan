package bestan.lua;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.google.gson.reflect.TypeToken;

import bestan.log.Glog;

public interface ILuaConfig {
	default void parseInt(LuaValue luaValue, Integer t) {
		t = luaValue.toint();
	}
	default void parseBool(LuaValue luaValue, Boolean t) {
		t = luaValue.toboolean();
	}
	default void parseShort(LuaValue luaValue, Short t) {
		t = luaValue.toshort();
	}
	default <T> void parseLuaConfig(LuaValue luaValue, T config) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
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
			Glog.trace("value_type={}",type.getType().getGenericSuperclass());
			parseLuaValue(tempLuaValue, value);
			it.set(config, value);
		}
	}
	default <T> void parseList(LuaValue luaValue, List<T> arr) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (!luaValue.istable()) return;
		
		var luaTable = (LuaTable)luaValue;
		for (var n = luaTable.next(LuaValue.NIL); !n.arg1().isnil(); n = luaTable.next(n.arg1())) {
			LuaValue key = n.arg1();
			LuaValue value = n.arg(2);
			//qdox-1.12.jar
			//Class tCls = method.getReturnType(); 
			//Glog.trace("xxxxx={}", tCls.getName());
			//var tempValue = (T)tCls.getDeclaredConstructor().newInstance();
			//parseLuaValue(luaValue, tempValue);
			//arr.add(tempValue);
		}
	}

	default <T> void parseLuaValue(LuaValue luaValue, T t) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		var cls = t.getClass();
		Glog.trace("parseLuaValue={}", cls.getName());
		var interfaces = cls.getInterfaces();
		Glog.trace("parseLuaValue={},{},{}", interfaces.length, interfaces[0].equals(ILuaConfig.class), interfaces[0] == ILuaConfig.class);
		if (interfaces.length > 0 && interfaces[0].equals(ILuaConfig.class)) {
			parseLuaConfig(luaValue, t);
		} else if (cls.equals(Integer.class)) {
			parseInt(luaValue, (Integer)t);
		} else if (cls.equals(Boolean.class)) {
			parseBool(luaValue, (Boolean)t);
		} else if (cls.equals(Short.class)) {
			parseShort(luaValue, (Short)t);
		} else if (interfaces.length > 0 && interfaces[0].equals(List.class)) {
			parseList(luaValue, (List)t);
		} else
		{	
			throw new LuaError("parse config failed, config=" + cls.getSimpleName() + ",super=" + cls.getSuperclass().getName());
		}
	}
	
	default void LoadLuaConfig(String path) {
		Globals globals = JsePlatform.standardGlobals();
		LuaValue chunk = globals.loadfile(path);
		var ret = chunk.call();
		try {
			Glog.trace("LoadLuaConfig={}", getClass().getSimpleName());
			parseLuaValue(ret, this);
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
