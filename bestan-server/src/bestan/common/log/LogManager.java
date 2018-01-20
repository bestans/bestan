package bestan.common.log;

import org.slf4j.LoggerFactory;

import bestan.common.reload.ReloadResult;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * This is a convenience class for initializing logback
 *
 * Please when using Log4j follow the next rules:<ul>
 * <li><b>debug</b> just debug info that may be need to find a bug.
 * <li><b>info</b> is just important information that we should be aware to spot behaviors on application.
 * <li><b>warn</b> is a problem that application will handle itself
 * <li><b>error</b> is a big problem on the application that it can't handle.
 * <li><b>fatal</b> is such a problem that the application will stop.
 * </ul>
 */
public class LogManager {
	/** flag indicating a successful configuration */
	private static boolean configured = false;

	/**
	 * initializes logback with a custom properties file.
	 *
	 * @param filename logback.xml
	 */
	public static boolean init() {
		if (configured) {
			return false;
		}
		
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure("config/logback.xml");
       } catch (JoranException e) {
            e.printStackTrace();
            StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
            return false;
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
        
        return true;
	}

	/**
	 * returns a logger for the given class. Use this function instead of
	 * <code>Logger.getLogger(clazz);</code>. If the logging mechanism
	 * changes it will be done here and not in every class using a logger.
	 *
	 * @param clazz
	 *            the Class requesting a logger
	 * @return the logger instance
	 */
	public static bestan.common.log.Logger getLogger(Class<?> clazz) {
		return new bestan.common.log.Logger(clazz);
	}
	
	/**
	 * 热部署
	 */
	public static ReloadResult reload() throws Exception {
		ReloadResult ret = new ReloadResult(true);
		ret.setSuccess(init());
		if(!ret.isSuccess()) {
			ret.appendMsg("reload logback.xml failed");
		} else {
			ret.appendMsg("reload logback.xml ok");
		}
		
		return ret;
	}
}
