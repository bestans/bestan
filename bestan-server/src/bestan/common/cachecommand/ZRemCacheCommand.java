package bestan.common.cachecommand;

import java.io.IOException;

import bestan.common.cache.CacheCommand;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class ZRemCacheCommand extends CacheCommand implements ICachePipeline {
	public final String key;
	public final String[] values;
	
	public ZRemCacheCommand(String key, String[] values) {
		this.key = key;
		this.values = values;
	}
	
	public ZRemCacheCommand(String key, String value) {
		this.key = key;
		String[] tmpArr = new String[1];
		tmpArr[0] = value;
		this.values = tmpArr;
	}
	
	public void pipelineExcute(Pipeline p) {
		DAORegister.get().get(CacheCommonDAO.class).zrem(p, key, values);
	}

	@Override
	public void execute(Jedis jedis) throws IOException {
		DAORegister.get().get(CacheCommonDAO.class).zrem(jedis, key, values);
	}

}
