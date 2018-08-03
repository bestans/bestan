package bestan.common.lua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LuaAnnotation(optional=true)
public class TestLua implements ILuaConfig {
	public int test = 11;
	public List<Integer> nums = new ArrayList<Integer>();
	public Map<Integer, ValueLua> map = new HashMap<>();
	public List<ValueLua> lists = new ArrayList<>();
	public String string = "aaa";
	public Map<Integer, Map<Integer, Integer>> dmap = new HashMap<>();
	public List<Map<Integer, Map<Integer, Map<Integer, Integer>>>> dList = new ArrayList<>();
	@LuaParamAnnotation(optional=true)
	public long lvalue = 122313L;
	
	public static class ValueLua implements ILuaConfig{
		public Integer v1 = 0;
		public Integer v2 = 0;
	}
}
