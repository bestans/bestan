package bestan.common.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import bestan.common.log.Glog;

public class BRejectedExecutionHandler implements RejectedExecutionHandler {
	@Override
	public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
		Glog.error("rejectedExecution={},{}", arg0, Thread.currentThread().getName());
	}
}
