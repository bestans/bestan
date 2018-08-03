package bestan.common.log;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Priority;

public class MyAppender extends FileAppender {
	@Override
	public boolean isAsSevereAsThreshold(Priority priority) {
		// 只判断是否相等，而不判断优先级
        return this.getThreshold().equals(priority);
    }
}
