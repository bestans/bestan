package bestan.common.net.handler;

import bestan.common.download.FileManager;
import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.protobuf.Proto.UpdateFileReq;
import bestan.common.protobuf.Proto.UpdateFileRes;
import bestan.common.protobuf.Proto.UpdateFileRes.RESULT;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author yeyouhuan
 *
 */
public class UpdateFileReqHandler implements IMessageHandler {

	@Override
	public void processProtocol(AbstractProtocol protocol) throws Exception {
		var req = (UpdateFileReq)protocol.getMessage();
		var ctx = protocol.getChannelHandlerContext();
		var updateList = FileManager.INSTANCE.getUpdateList(req.getFilesMap());
		if (updateList == null) {
			//已经更新完毕了
			var resBuilder = UpdateFileRes.newBuilder();
			resBuilder.setRetcode(RESULT.FINISH_DOWNLOAD);
			resBuilder.setNoChange(true);
			var res = resBuilder.build();
			//TODO
			var futhure = ctx.writeAndFlush(res);
			futhure.addListener(new UpdateFuture(res));
			return;
		}
		
		ctx.pipeline().remove("encoder");
		ctx.pipeline().addBefore("serverHandler", "chunk", new ChunkedWriteHandler());
		
		for (var it : updateList) {
			ctx.write(it);
		}
	}

	class UpdateFuture implements ChannelFutureListener {
		private UpdateFileRes res;
		
		UpdateFuture(UpdateFileRes res) {
			this.res = res;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			Glog.debug("UpdateFileReqHandler:operationComplete:res={},futureResult={}", res, future.isSuccess());
			
		}
	}
}
