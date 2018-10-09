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
		// TODO Auto-generated method stub
		var curVersion = ctx.attr(NetConst.UPDATE_ATTR_INDEX);
		if (curVersion == null || curVersion.get() != version) {
			return true;
		}
		return false;
	}
}
