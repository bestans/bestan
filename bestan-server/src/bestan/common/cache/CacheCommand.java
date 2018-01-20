package bestan.common.cache;

import java.io.IOException;

import redis.clients.jedis.Jedis;

public abstract class CacheCommand implements ICacheCommand {
	private Exception exception = null;

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public abstract void execute(Jedis jedis) throws IOException;
}
