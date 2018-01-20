package bestan.common.db.command;

/**
 * identifies the results belonging to the code doing the request
 */
public final class ResultHandle {

	private String random;

	/**
	 * creates a new result handle
	 */
	public ResultHandle() {
		random = Thread.currentThread().getName() + "-" + System.currentTimeMillis() + "-" + Math.random();
	}

	@Override
	public String toString() {
		return "ResultHandle [" + random + "]";
	}

}
