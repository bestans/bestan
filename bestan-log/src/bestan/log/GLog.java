package bestan.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GLog {
	public static final Logger log = LoggerFactory.getLogger("myDebug");
	public static Logger GetLog()
	{
		return log;
	}
}
