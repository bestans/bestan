package bestan.common.util;

/**
 * @author yeyouhuan
 *
 */
public class ExceptionUtil {
	public static String getLog(Throwable e) {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("[[message=").append(e.getMessage());
		stringBuilder.append(",trace=");
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement traceElement : trace)
            stringBuilder.append("\n\tat ").append(traceElement);
        
        stringBuilder.append("]]");
		return stringBuilder.toString();
	}
}
