package bestan.test.server;

import bestan.common.event.IEvent;
import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.thread.BExecutor;

public class TestExecutor implements BExecutor {

	@Override
	public void execute(IEvent event) {
		// TODO Auto-generated method stub
		if  (event instanceof AbstractProtocol) {
			var p = (AbstractProtocol)event;
			Glog.debug("event={}", p.getMessage());
		}
	}

}
