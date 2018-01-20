package bestan.common.cache;

import java.io.IOException;

import redis.clients.jedis.Jedis;

public interface ICacheCommand {
	public Exception getException();

	public void execute(Jedis jedis) throws IOException;

	public void setException(Exception exception);
}
