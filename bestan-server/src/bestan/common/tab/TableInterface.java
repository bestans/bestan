package bestan.common.tab;

import bestan.common.exception.TableLoadException;

public abstract class TableInterface {
	public TableInterface() { 
		try {
			mapData(TableRow.g_ClearSerializer);
		} catch (TableLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	abstract public void mapData(ISerializer s) throws TableLoadException;
	
	private int index;
	public int getIndex() { return index; }
	public int mapIndex(ISerializer s) throws TableLoadException{
		this.index = CommonTableInterface.ParseInt(s);
		s.SetCurrentID(index);
		return index;
	}
}
