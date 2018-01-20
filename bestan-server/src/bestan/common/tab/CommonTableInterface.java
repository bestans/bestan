package bestan.common.tab;

import bestan.common.exception.TableLoadException;
import bestan.common.util.Global;

public class CommonTableInterface extends TableInterface {
	public static CommonTableInterface instance = new CommonTableInterface();
	
	Object m_TempValue;

	public void setValue(int arr){
		m_TempValue = arr;
	}

	public void setValue(int[] arr){
		m_TempValue = arr;
	}

	public void setValue(float arr){
		m_TempValue = arr;
	}

	public void setValue(float[] arr){
		m_TempValue = arr;
	}
	public void setValue(String arr){
		m_TempValue = arr;
	}	
	
	@Override
	public void mapData(ISerializer s) throws TableLoadException {
		//
	}
	
	public static String ParseString(ISerializer s) throws TableLoadException{
		instance.m_TempValue = null;
		s.Parse(instance, "setValue", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_STRING);
		if (null == instance.m_TempValue) {
			return Global.INVALID_STRING;
		}
		return (String)instance.m_TempValue;
	}
	
	public static int ParseInt(ISerializer s) throws TableLoadException{
		instance.m_TempValue = null;
		s.Parse(instance, "setValue", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_INT);
		if (null == instance.m_TempValue){
			return Global.INVALID_VALUE;
		}
		return ((Integer)instance.m_TempValue).intValue();
	}
	
	public static float ParseFloat(ISerializer s) throws TableLoadException{
		instance.m_TempValue = null;
		s.Parse(instance, "setValue", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_FLOAT);
		if (null == instance.m_TempValue){
			return Global.INVALID_FLOAT_VALUE;
		}
		return ((Float)instance.m_TempValue).floatValue();
	}

	public static int[] ParseIntArray(ISerializer s) throws TableLoadException{
		instance.m_TempValue = null;
		s.Parse(instance, "setValue", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_INT_ARRAY);
		if (null == instance.m_TempValue){
			return null;
		}
		return (int[])instance.m_TempValue;
	}

	public static float[] ParseFloatArray(ISerializer s) throws TableLoadException{
		instance.m_TempValue = null;
		s.Parse(instance, "setValue", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_FLOAT_ARRAY);
		if (null == instance.m_TempValue){
			return null;
		}
		return (float[])instance.m_TempValue;
	}
}
