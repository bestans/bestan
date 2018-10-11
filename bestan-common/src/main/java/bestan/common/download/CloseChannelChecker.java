package bestan.common.download;

import bestan.common.net.ICloseChannelChecker;
import bestan.common.net.NetConst;
import io.netty.channel.Channel;

/**
 * @author yeyouhuan
 *
 */
public class CloseChannelChecker implements ICloseChannelChecker {
	private int version;
	
	public CloseChannelChecker(int version) {
		this.version = version;
	}
	
	@Override
	public boolean checkCanClose(Channel ctx) {
		if (!ctx.hasAttr(NetConst.UPDATE_ATTR_INDEX)) {
			return true;
		}
		if (ctx.attr(NetConst.UPDATE_ATTR_INDEX).get().getVersion() != version) {
			return true;
		}
		return false;
	}
}
