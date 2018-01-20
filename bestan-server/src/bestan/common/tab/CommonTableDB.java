package bestan.common.tab;

import java.io.File;

import bestan.common.exception.TableLoadException;
import bestan.common.reload.IHotdeploy;
import bestan.common.util.Global;


public abstract class CommonTableDB implements IHotdeploy {
	public static final String RootPath = Global.TABLE_PATH + File.separatorChar;
    
    protected CommonTableDB() { }
    
    // timer tab
    public Table<TableTimer> timerConfigTable = new Table<TableTimer>(TableTimer.class);
    
    // HotDeploy.tab
    public Table<TableHotDeployConfig> hotDeployConfigTable = new Table<TableHotDeployConfig>(TableHotDeployConfig.class);
    
    public abstract void load() throws TableLoadException;
    
    protected abstract void excuteLoadByName(String tabName) throws TableLoadException;
}
