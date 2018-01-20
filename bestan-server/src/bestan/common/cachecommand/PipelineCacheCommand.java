package bestan.common.cachecommand;

import java.io.IOException;

import bestan.common.cache.CacheCommand;
import redis.clients.jedis.Jedis;
import bestan.common.cachecommand.CacheCommonDAO;
import bestan.common.dao.DAORegister;

public class PipelineCacheCommand extends CacheCommand {
	public ICachePipeline[] operationArray;
	
	public PipelineCacheCommand(ICachePipeline[] operationArray) {
		this.operationArray = operationArray; 
	}
	
	@Override
	public void execute(Jedis jedis) throws IOException {
		DAORegister.get().get(CacheCommonDAO.class).pipelineOperation(jedis, operationArray);
	}
	
}
