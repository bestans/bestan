package bestan.common.net;

import io.netty.channel.Channel;

/**
 * @author yeyouhuan
 *
 */
public interface ICloseChannelChecker {
	boolean checkCanClose(Channel ctx);
}
