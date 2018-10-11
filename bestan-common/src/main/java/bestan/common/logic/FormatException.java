package bestan.common.logic;

/**
 * @author yeyouhuan
 *
 */
public class FormatException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5416430845387238273L;

	public FormatException(String format, Object... args) {
		super(getMessage(format, args));
	}
	public FormatException(String format) {
		super(format);
	}
	
	private static String getMessage(String format, Object... args) {
		String messasge;
		try {
			messasge = String.format(format, args);
		} catch (Exception e) {
			messasge = "FormatException:format=" + format + ",exception=" + e.getMessage();
		}
		
		return messasge;
	}
}
