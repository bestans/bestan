package bestan.common.cachecommand;

import java.io.IOException;

import bestan.common.cache.CacheCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class MSetCacheCommand extends CacheCommand implements ICachePipeline {
	public final String[] keyvalues;
	
	public MSetCacheCommand(String[] keyvalues) {
		this.keyvalues = keyvalues;
	}

	@Override
	public void execute(Jedis jedis) throws IOException {
		DAORegister.get().get(CacheCommonDAO.class).mset(jedis, keyvalues);
	}

	public void pipelineExcute(Pipeline p) {
		DAORegister.get().get(CacheCommonDAO.class).mset(p, keyvalues);
	}
	
}