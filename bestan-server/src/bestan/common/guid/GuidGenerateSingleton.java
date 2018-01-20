/**
 * Class: guidgenerate.java
 * Package:
 * 
 * @author lvbeibei
 * Copyright (c) 2014, bestan All Rights Reserved.
*/
package bestan.common.guid;

import java.io.IOException;

import bestan.common.config.ServerConfig;
import bestan.common.util.Global;
import bestan.common.util.SeqIdUtil;

/**
 * guid guidgenerate singleton class
 * 
 * @author lvbeibei	
 */

public class GuidGenerateSingleton {	
		
	private Guid guidarray[];
	
	private static GuidGenerateSingleton guidgenerate = null;
	
	private GuidGenerateSingleton(){
	}
	
	public static GuidGenerateSingleton getSingleton(){
		if (guidgenerate == null) {
			guidgenerate = new GuidGenerateSingleton();
		}
		return guidgenerate;
	}
	
	public boolean init() throws IOException{
		if (guidgenerate == null) {
			return false;
		}
		
		guidarray = new Guid[Global.OBJECT_TYPE_NUMBER];
		for (int i = 0; i < guidarray.length; ++i) {
			guidarray[i] = new Guid();
		}

		short loginServerId = (short)ServerConfig.getInstance().getLoginServerId();
		for (short type = 0; type < Global.OBJECT_TYPE_NUMBER; ++type) {
			guidarray[type].setLoginServId(loginServerId);
			guidarray[type].setType(type);
			switch (type) {
			case Global.OBJECT_TYPE_PLAYER:
			case Global.OBJECT_TYPE_TOWER:
			case Global.OBJECT_TYPE_CONNECTION:
			case Global.OBJECT_TYPE_HERO: 
			case Global.OBJECT_TYPE_ITEM:
			case Global.OBJECT_TYPE_MOVIE: {
				guidarray[type].setId(0);
			}
				break;
			default:
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 生成player guid
	 * 
	 * @param subType
	 * @return
	 */
	public Guid generaterPlayerGuid(short subType){
		if (guidgenerate == null) {
			return null;
		}
		Guid generaterguid = new Guid(guidarray[Global.OBJECT_TYPE_PLAYER].getValue());
		generaterguid.setSubType(subType);
		generaterguid.setId(SeqIdUtil.getPlayerSeqID());
		return generaterguid;
	}
	
	/**
	 * 生成player guid
	 * 
	 * @param loginServerId
	 * @param subType
	 * @return
	 */
	public Guid generaterPlayerGuid(short loginServerId, short subType){
		if (guidgenerate == null) {
			return null;
		}
		Guid generaterguid = new Guid(guidarray[Global.OBJECT_TYPE_PLAYER].getValue());
		generaterguid.setLoginServId(loginServerId);
		generaterguid.setSubType(subType);
		generaterguid.setId(SeqIdUtil.getPlayerSeqID());
		return generaterguid;
	}
	
	/**
	 * 生成临时 guid
	 * 
	 * @param name
	 * @return
	 */
	public Guid generaterTempGuid(int id){
		if (guidgenerate == null) {
			return null;
		}

		Guid generaterguid = new Guid(guidarray[Global.OBJECT_TYPE_PLAYER].getValue());
		generaterguid.setSubType((short)Global.OBJECT_SUBTYPE_TEMP);
		generaterguid.setId(id);
		return generaterguid;
	}
} 