package bestan.gameserver.handler;

import com.google.protobuf.Message;

import bestan.gameserver.net.MessageHandler;
import bestan.gameserver.object.Player;
import bestan.log.GLog;
import bestan.pb.NetCommon.test_data;
import io.netty.channel.ChannelHandlerContext;

public class CSTestDataHandler extends MessageHandler {
	@Override
	protected void process(ChannelHandlerContext ctx, Message message, Player pPlayer) {
		test_data tData = (test_data)message;
		GLog.log.debug("process_yyh:{}", tData.getValue());
		
		writeResponse(ctx, tData);
	}
}
