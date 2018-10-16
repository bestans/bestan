package bestan.test.clientdownload;

import bestan.common.net.AbstractProtocol;
import bestan.common.net.handler.IMessageHandler;
import bestan.common.net.handler.NoteMessageHandler;
import bestan.common.protobuf.Proto.ChunkedData;
import bestan.test.clientdownload.UpdateFileResHandler.UpdateFile;

/**
 * @author yeyouhuan
 *
 */
@NoteMessageHandler(messageName="ChunkedData")
public class ChunkedDataHandler implements IMessageHandler {

	@Override
	public void processProtocol(AbstractProtocol protocol) throws Exception {
		var data = (ChunkedData)protocol.getMessage();
		UpdateFile.getInstance().onRecvData(data);
	}
}
