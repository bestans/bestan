package bestan.common.cachecommand;

import java.io.IOException;

import bestan.common.cache.CacheCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class SetCacheCommand extends CacheCommand implements ICachePipeline {
	public final String key;
	public final String value;
	
	public SetCacheCommand(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public void execute(Jedis jedis) throws IOException {
		DAORegister.get().get(CacheCommonDAO.class).set(jedis, key, value);
	}

	public void pipelineExcute(Pipeline p) {
		DAORegister.get().get(CacheCommonDAO.class).set(p, key, value);
	}
	
}