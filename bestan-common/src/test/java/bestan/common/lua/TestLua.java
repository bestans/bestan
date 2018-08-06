package bestan.common.lua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bestan.common.lua.LuaParamAnnotation.LuaParamPolicy;

@LuaAnnotation(optional=true)
public class TestLua extends BaseLuaConfig {
	public int test = 11;
	public List<Integer> nums = new ArrayList<Integer>();
	public Map<Integer, ValueLua> map = new HashMap<>();
	public List<ValueLua> lists = new ArrayList<>();
	public String string = "aaa";
	public Map<Integer, Map<Integer, Integer>> dmap = new HashMap<>();
	public List<Map<Integer, Map<Integer, Map<Integer, Integer>>>> dList = new ArrayList<>();
	@LuaParamAnnotation(policy=LuaParamPolicy.OPTIONAL)
	public long lvalue = 122313L;
	
	public static class ValueLua extends BaseLuaConfig{
		public Integer v1 = 0;
		public Integer v2 = 0;
	}
}
