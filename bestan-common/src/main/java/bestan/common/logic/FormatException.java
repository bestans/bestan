package bestan.common.logic;

/**
 * @author yeyouhuan
 *
 */
public class FormatException extends Exception {
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
