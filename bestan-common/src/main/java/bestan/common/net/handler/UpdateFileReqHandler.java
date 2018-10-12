package bestan.common.net.handler;

import bestan.common.download.FileManager;
import bestan.common.download.UpdateState;
import bestan.common.download.UpdateState.STATE;
import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.NetConst;
import bestan.common.net.chunk.MessageChunkedInput;
import bestan.common.protobuf.Proto.UpdateFileReq;
import bestan.common.protobuf.Proto.UpdateFileReq.REQ_TYPE;
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
	private static UpdateFuture updateFuture = new UpdateFuture();
	
	@Override
	public void processProtocol(AbstractProtocol protocol) throws Exception {
		var req = (UpdateFileReq)protocol.getMessage();
		var ctx = protocol.getChannelHandlerContext();
		var resource = FileManager.getInstance().getResource();
		//初始化下载状态
		if (!ctx.channel().hasAttr(NetConst.UPDATE_ATTR_INDEX)) {
			if (req.getReq() != REQ_TYPE.REQUEST) {
				ctx.close();
				return;
			}
			ctx.channel().attr(NetConst.UPDATE_ATTR_INDEX).set(new UpdateState(resource.getUpdateList(req.getFilesMap())));
		}
		var state = ctx.channel().attr(NetConst.UPDATE_ATTR_INDEX).get();
		switch (req.getReq()) {
		case REQUEST:	//请求下载
			if (state.getState() != STATE.REQ) {
				return;
			}
			onRequest(req, ctx, state);
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
	
	public void onRequest(UpdateFileReq req, ChannelHandlerContext ctx, UpdateState state) {
		var updateList = state.getUpdateList();
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
			futhure.addListener(updateFuture);
			return;
		}

		//通知客户端准备下载了
		var resBuilder = UpdateFileRes.newBuilder();
		resBuilder.setRetcode(RESULT.START_DOWNLOAD);
		ctx.writeAndFlush(resBuilder.build());
		
		state.setState(STATE.WAIT_PREPARE);
	}

	public void onDownload(ChannelHandlerContext ctx, UpdateState state) {
		var updateList = state.getUpdateList();
		
		ctx.pipeline().remove("encoder");
		ctx.pipeline().addBefore("serverHandler", "chunk", new ChunkedWriteHandler());
	
		var f = new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					future.channel().close();
				}
			}
		};
		for (var it : updateList) {
			ctx.write(new MessageChunkedInput(it.getFileData())).addListener(f);
		}

		//结束
		var resBuilder = UpdateFileRes.newBuilder();
		resBuilder.setRetcode(RESULT.FINISH_DOWNLOAD);
		ctx.writeAndFlush(resBuilder.build()).addListener(updateFuture);
	}
	
	static class UpdateFuture implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			Glog.debug("UpdateFileReqHandler:operationComplete:result={}", future.isSuccess());
		}
	}
	
}
