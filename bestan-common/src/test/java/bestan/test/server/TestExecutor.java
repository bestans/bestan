package bestan.test.server;

import bestan.common.event.IEvent;
import bestan.common.log.Glog;
import bestan.common.net.CommonProtocol;
import bestan.common.thread.BExecutor;

public class TestExecutor implements BExecutor {

	@Override
	public void execute(IEvent event) {
		// TODO Auto-generated method stub
		if  (event instanceof CommonProtocol) {
			var p = (CommonProtocol)event;
			Glog.debug("event={}", p.getMessage());
		}
	}

}
