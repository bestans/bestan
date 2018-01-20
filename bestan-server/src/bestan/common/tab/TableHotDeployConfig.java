package bestan.common.tab;

import bestan.common.exception.TableLoadException;

public class TableHotDeployConfig extends TableInterface
{
	public static final int HOTUPDATE_TYPE_TAB = 1;
	public static final int HOTUPDATE_TYPE_CONFIG = 2;
	public static final int HOTUPDATE_TYPE_CLAZZ = 3;
	
	/** id */
    public int id;
    
    /** 类型 */
    public int type;
    
    /** 内容 */
    public String content;

    /**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param clazz the clazz to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	@Override
    public void mapData(ISerializer s) throws TableLoadException
    {
		this.id = super.mapIndex(s);
        s.Parse(this, "setType", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_INT);
        s.Parse(this, "setContent", TableSerializer.EM_TYPE_COLUMN.EM_TYPE_COLUMN_STRING);
    }
}
