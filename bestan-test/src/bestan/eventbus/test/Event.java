package bestan.eventbus.test;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

import bestan.common.log.Glog;

public class Event {
	@Subscribe
	public void sub(String message) {
		Glog.debug("event={}{}", message, Thread.currentThread().getId());
	}
	
	@Subscribe
	public void onDead(DeadEvent e) {
		Glog.debug("onDead={}", e.getEvent());
	}
}
