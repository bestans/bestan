package bestan.common.log;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.LoggerFactory;

public class Logger {

	/** The log4j instance */
	org.slf4j.Logger _logger;

	Logger(Class<?> arg0) {

		_logger = LoggerFactory.getLogger(arg0);
	}

	static Logger getLogger(Class<?> clazz) {
		return new Logger(clazz);
	}
	
	public boolean isDebugEnabled() {

		return _logger.isDebugEnabled();
	}

	public void debug(String arg0, Throwable arg1) {

		_logger.debug(arg0, arg1);
	}

	public void debug(String arg0) {

		_logger.debug(arg0);
	}

	public boolean isInfoEnabled() {

		return _logger.isInfoEnabled();
	}
	
	public void info(String arg0, Throwable arg1) {

		_logger.info(arg0, arg1);
	}

	public void info(String arg0) {

		_logger.info(arg0);
	}
	
	public void warn(String arg0, Throwable arg1) {

		_logger.warn(arg0, arg1);
	}

	public void warn(String arg0) {

		_logger.warn(arg0);
	}
	
	public void error(String arg0, Throwable arg1) {

		_logger.error(arg0, arg1);
	}

	public void error(String arg0) {

		_logger.error(arg0);
	}
	
	/**
	 * print stack of exception
	 * @param e
	 */
	public void printStackTraceToLog(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		error(sw.toString());
	}
}
