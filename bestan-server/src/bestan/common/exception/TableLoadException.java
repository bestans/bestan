package bestan.common.exception;

public class TableLoadException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1571199116911249953L;

	/**
	 * creates a new TableLoadException
	 */
	public TableLoadException(String tabName) {
		super("Load Table: " + tabName + " format is invalid.");
	}
	
	public TableLoadException(String tabName, String msg) {
		super("Load Table: " + tabName + " " + msg);
	}
}
