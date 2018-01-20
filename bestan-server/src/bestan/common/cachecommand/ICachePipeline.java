package bestan.common.cachecommand;

import redis.clients.jedis.Pipeline;

public interface ICachePipeline {
	public void pipelineExcute(Pipeline p);
}
