package bestan.test.clientdownload;

import java.io.File;
import java.io.IOException;
import java.util.List;

import bestan.common.log.Glog;
import bestan.common.net.AbstractProtocol;
import bestan.common.net.client.BaseNetClientManager;
import bestan.common.net.handler.IMessageHandler;
import bestan.common.net.handler.NoteMessageHandler;
import bestan.common.protobuf.Proto.ChunkedData;
import bestan.common.protobuf.Proto.FileInfo;
import bestan.common.protobuf.Proto.UpdateFileReq;
import bestan.common.protobuf.Proto.UpdateFileReq.REQ_TYPE;
import bestan.common.protobuf.Proto.UpdateFileRes;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author yeyouhuan
 *
 */
@NoteMessageHandler(messageName="UpdateFileRes")
public class UpdateFileResHandler implements IMessageHandler {
	public static String path = "E:/bestan/config/downloadres";
	
	@Override
	public void processProtocol(AbstractProtocol protocol) throws Exception {
		var res = (UpdateFileRes)protocol.getMessage();
		Glog.debug("UpdateFileResHandler = {}", res);
		
		UpdateFile.getInstance().onRecvRes(res, protocol.getChannelHandlerContext(), protocol);
	}

	public static class UpdateFile {
		private List<FileInfo> fileList;
		private int index = 0;
		private boolean newFile = true;
		
		private static UpdateFile INSTANCE = new UpdateFile();
		public static UpdateFile getInstance() {
			return INSTANCE;
		}
		
		public void request(BaseNetClientManager net) {
			var req = UpdateFileReq.newBuilder();
			req.setReq(REQ_TYPE.REQUEST);
			net.writeAndFlush(req.build());
		}
		public void onRecvRes(UpdateFileRes res, ChannelHandlerContext ctx, AbstractProtocol protocol) {
			switch (res.getRetcode()) {
			case FINISH_DOWNLOAD:
				break;
			case NO_CHANGE:
				break;
			case START_DOWNLOAD:
				fileList = res.getAllChangeFilesList();
				index = 0;
				newFile = true;
				
				var req = UpdateFileReq.newBuilder();
				req.setReq(REQ_TYPE.PREPARE);
				ctx.writeAndFlush(protocol.packMessage(req.build()));
				break;
			default:
				break;
			}
		}
		
		public void onRecvData(ChunkedData data) {
			var fileInfo = fileList.get(index);
			String filepath = path + fileInfo.getBaseInfo().getFileName();
			Glog.debug("onRecvData filepath={}, data={}",filepath, data);
			if (data.getEnd()) {
				++index;
				newFile = true;
				return;
			}
			var file = new File(filepath);
			try {
				org.apache.commons.io.FileUtils.writeByteArrayToFile(file, data.getChunk().toByteArray(), !newFile);
				newFile = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
