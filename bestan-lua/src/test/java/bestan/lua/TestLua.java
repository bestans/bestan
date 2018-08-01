package bestan.lua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestLua implements ILuaConfig {
	public Integer test = 11;
	public List<Integer> nums = new ArrayList<Integer>();
	public Map<Integer, ValueLua> map = new HashMap<>();
	public List<ValueLua> lists = new ArrayList<>();
	public String string = "";
	
	public static class ValueLua implements ILuaConfig{
		public Integer v1 = 0;
		public Integer v2 = 0;
	}
}
