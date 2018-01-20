/***************************************************************************
 * class  : NetModeAdapter
 * @author: lixiwen
 * 
 * desc   : net mode adapter can use Nio(jdk) or Netty
 *          will modified template later
 ***************************************************************************/

package bestan.common.net;

import io.netty.channel.ChannelHandlerContext;

import java.nio.channels.SocketChannel;

import bestan.common.log.LogManager;
import bestan.common.log.Logger;
import bestan.common.net.netty.NettyServer;
import bestan.common.net.nio.server.DispatchServerManager;
import bestan.common.net.nio.server.INetworkServerManager;
import bestan.common.server.MainServer.E_SERVER_TYPE;

import com.google.protobuf.GeneratedMessage;

public class NetModeAdapter {
	/** the logger instance. */
	private static final Logger logger = LogManager.getLogger(NetModeAdapter.class);
	
	/** A network manager object to handle network events */
	private INetworkServerManager netMan;
	/** A game manager object to handle server glue logic and database stuff */
	private DispatchServerManager[] gameMan;
	
	private NettyServer netttyMan;
	
	private String netName;
	
	public NetModeAdapter(String netName) {
		cleanUp();
		this.netName = netName;
		init();
	}
	
	private void cleanUp() {
		this.netName = null;
		this.netMan = null;
		this.netttyMan = null;
	}
	
	private void init() {
		try {
			if(this.netName.equalsIgnoreCase("nio")) {
				netMan = new bestan.common.net.nio.server.NIONetworkServerManager();
			} else if(this.netName.equalsIgnoreCase("netty")) {
				netttyMan = new NettyServer(E_SERVER_TYPE.GameServer);
			} else {
				logger.error("Dotata no NetMode[" + this.netName +"] server will be exited");
				System.exit(1);
			}
		} catch(Exception e) {
			logger.error("Dotata can't create NetworkServerManager.\n" + "NetMode:" + this.netName);
			e.printStackTrace();
		}
	}
	
	public void start() {
		if(this.netName.equalsIgnoreCase("nio")) {
			netMan.start();
			
			//=========================================================================
			// game server manager
			try {
				gameMan = new DispatchServerManager[netMan.getProcessCount()];
				for(int i = 0; i < netMan.getProcessCount(); i++) {
					gameMan[i] = new DispatchServerManager(netMan, i);
					gameMan[i].start();
				}
				
			} catch (Exception e) {
				logger.error(
								"Dotata can't create GameServerManager. With Nio NetMode\n"
										+ "Reasons:\n"
										+ "- You haven't specified a valid configuration file\n"
										+ "- You haven't correctly filled the values related to server information configuration. Use generateini application to create a valid configuration file.\n",
								e);
				System.exit(1);
			}
			//=========================================================================
		} else if(this.netName.equalsIgnoreCase("netty")) {
			try {
				netttyMan.start();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			logger.error("NetMode[" + this.netName + "] is invalid. start failed");
			System.exit(1);
		}
	}
	
	public DispatchServerManager getGameServerManager(int index) {
		if(index >= netMan.getProcessCount() || index < 0) {
			return null;
		}
		
		return gameMan[index];
	}
	
	/**
	 * shuts down
	 */
	public void finish() throws Exception {
		if (this.netName.equalsIgnoreCase("nio")) {
			int nLogicCnt = netMan.getProcessCount();
			netMan.finish();
			for (int i = 0; i < nLogicCnt; i++) {
				gameMan[i].finish();
			}
		} else if(this.netName.equalsIgnoreCase("netty")) {
			this.netttyMan.stopServer();
		}
	}
	
	/**
	 * @param msg
	 *            the GeneratedMessage to be delivered.
	 */
	public void sendMessage(GeneratedMessage msg, Object channel) {
		if(channel instanceof SocketChannel) {
			this.netMan.sendMessage(msg, (SocketChannel)channel);
		} else if(channel instanceof ChannelHandlerContext) {
			ChannelHandlerContext ctx = (ChannelHandlerContext) channel;
			ctx.writeAndFlush(msg);
		}
	}
}
