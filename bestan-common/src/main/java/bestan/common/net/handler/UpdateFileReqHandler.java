package bestan.common.net.handler;

import bestan.common.download.FileManager;
import bestan.common.download.FileResource;
import bestan.common.download.UpdateState;
import bestan.common.download.UpdateState.STATE;
import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.NetConst;
import bestan.common.protobuf.Proto.UpdateFileReq;
import bestan.common.protobuf.Proto.UpdateFileRes;
import bestan.common.protobuf.Proto.UpdateFileRes.RESULT;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
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
		var resource = FileManager.INSTANCE.getResource();
		//初始化下载状态
		if (!ctx.channel().hasAttr(NetConst.UPDATE_ATTR_INDEX)) {
			ctx.channel().attr(NetConst.UPDATE_ATTR_INDEX).set(new UpdateState(resource.getVersion()));
		}
		var state = ctx.channel().attr(NetConst.UPDATE_ATTR_INDEX).get();
		switch (req.getReq()) {
		case REQUEST:	//请求下载
			if (state.getState() != STATE.REQ) {
				return;
			}
			onRequest(req, ctx, resource, state);
			break;
		case PREPARE:	//对方准备好了，开始下载
			if (state.getState() != STATE.WAIT_PREPARE) {
				return;
			}
			state.setState(STATE.IN_DOWNLOAD);
			onDownload(ctx, state);
			break;
		default:
			ctx.close();
			return;
		}
	}
	
	public void onRequest(UpdateFileReq req, ChannelHandlerContext ctx, FileResource resource, UpdateState state) {
		var updateList = resource.getUpdateList(req.getFilesMap());
		if (updateList == null) {
			//设置已下载完成
			state.setState(STATE.FINISH);
			
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

		//通知客户端准备下载了
		var resBuilder = UpdateFileRes.newBuilder();
		resBuilder.setRetcode(RESULT.START_DOWNLOAD);
		ctx.writeAndFlush(resBuilder.build());

		state.setResource(resource);
		state.setUpdateList(updateList);
		state.setState(STATE.WAIT_PREPARE);
	}

	public void onDownload(ChannelHandlerContext ctx, UpdateState state) {
		var resource = state.getResource();
		var updateList = state.getUpdateList();
		
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
