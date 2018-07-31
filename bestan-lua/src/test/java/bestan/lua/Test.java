package bestan.lua;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import bestan.log.Glog;

public class Test {
	public static void test1() {
		Globals globals = JsePlatform.standardGlobals();
		LuaValue chunk = globals.loadfile("test.lua");
		var ret = chunk.call();
		var tvalue = ret.get("test");
		if (tvalue != null) {
			Glog.trace("bbbb={}", tvalue.toint());
		}
	}
	
	public static void test2() {
		var cfg = new TestLua();
		cfg.LoadLuaConfig("test.lua");
	}

	public static void main(String[] args) {
		int i = 1;
		Method temp = null;
		while (true) {
			Method cur = null;
			try {
				cur = Test.class.getMethod("test" + i++);
			} catch (NoSuchMethodException e) {
				//e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (cur == null) {
				break;
			}
			else {
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
