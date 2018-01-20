package bestan.common.tab;

import bestan.common.exception.TableLoadException;
import bestan.common.tab.TableSerializer.EM_TYPE_COLUMN;

public interface ISerializer {
     public abstract ISerializer Parse(TableInterface t, String func, EM_TYPE_COLUMN eType) throws TableLoadException;

     public abstract void SkipField();
     public abstract void SetCheckColumn(boolean isCheck);   
     public abstract void SetCurrentID(int id);
}
