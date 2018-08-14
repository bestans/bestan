package bestan.common.timer;

import bestan.common.logic.BaseObject;

public interface ITimer {
	default void executeTick() {
		if (!validTimer()) {
			return;
		}
		if (this instanceof BaseObject) {
			var obj = (BaseObject)this;
			obj.lockObject();
			try {
				obj.Tick();
			} finally {
				obj.unlockObject();
			}
		}
	}

	default boolean validTimer() {
		return true;
	}
}
