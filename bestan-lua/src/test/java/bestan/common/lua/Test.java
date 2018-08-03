package bestan.common.lua;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import bestan.log.Glog;

public class Test {
    List<String> stringList = new ArrayList<String>();
    List<Integer> integerList = new ArrayList<Integer>();
    
	public static void test1() {
		Globals globals = JsePlatform.standardGlobals();
		LuaValue chunk = globals.loadfile("test.common.lua");
		var ret = chunk.call();
		var tvalue = ret.get("test");
		if (tvalue != null) {
			Glog.trace("bbbb={}", tvalue.toint());
		}
	}

	public static void test7() {
		LuaConfigs.loadConfig("bestan.common.lua");
		var cfg = LuaConfigs.get(TestSon.class);
		Glog.trace("test5={},{}", cfg.test, cfg.string);
		for (var it : cfg.nums) {
			System.out.println(it);
		}
		for (var it :cfg.map.entrySet()) {
			Glog.trace("key={},value={},{}", it.getKey(), it.getValue().v1, it.getValue().v2);
		}
		for (var it : cfg.lists) {
			Glog.trace("v={},{}", it.v1, it.v2);
		}
		for (var it : cfg.dmap.entrySet()) {
			Glog.trace("key={},value={}", it.getKey(), it.getValue());
		}
		Glog.trace("cfg.dList={},{}", cfg.dList, cfg.lvalue);
	}
	public static void test4() throws NoSuchFieldException, SecurityException {
        //Field stringListField = Test.class.getDeclaredField("stringList");
		for (var stringListField : TestLua.class.getFields()) {
			Glog.trace("testtttt={}", stringListField.getGenericType().getTypeName());
			var generic = stringListField.getGenericType();
			if (!(generic instanceof ParameterizedType))
				continue;
        ParameterizedType stringListType = (ParameterizedType) stringListField.getGenericType();
        for (var it : stringListType.getActualTypeArguments()) {
        	System.out.println(generic.getTypeName() +"," + it.getTypeName());
        }
//        Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
//        System.out.println(stringListClass.getName()+stringListType.getActualTypeArguments().length); // class java.lang.String.
		}

        Field integerListField = Test.class.getDeclaredField("integerList");
        ParameterizedType integerListType = (ParameterizedType) integerListField.getGenericType();
        Class<?> integerListClass = (Class<?>) integerListType.getActualTypeArguments()[0];
        System.out.println(integerListClass); // class java.lang.Integer.
	}
	
	public static void test3() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		var ob = (TestLua)(TestLua.class.getDeclaredConstructor().newInstance());
		System.out.println(ob.test);
	}
	public static void main(String[] args) {
		int i = 0;
		Method temp = null;
		while (++i < 100) {
			Method cur = null;
			try {
				cur = Test.class.getMethod("test" + i);
			} catch (NoSuchMethodException e) {
				//e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (cur != null) {
				temp = cur;
			}
		}
		if (temp != null) {
			try {
				temp.invoke(null);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
