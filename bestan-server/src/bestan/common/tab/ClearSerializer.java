package bestan.common.tab;

import bestan.common.tab.TableSerializer.EM_TYPE_COLUMN;

public class ClearSerializer implements ISerializer {
	public ClearSerializer() { }

	public ISerializer Parse(TableInterface t, String value, EM_TYPE_COLUMN eType) { return this; }

	public void SkipField() { return; }
    
	public void SetCheckColumn(boolean isCheck) { return; }
    
    public void SetCurrentID(int id){ return; }
}
