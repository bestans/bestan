package bestan.common.logic;

import bestan.common.net.AbstractProtocol;

public class CommonCallback implements ICallback<AbstractProtocol>{
	protected IObject srcObject;
	protected IObject dstObject;
	protected AbstractProtocol arg;
	protected AbstractProtocol res;
	
	public CommonCallback(IObject src, IObject dst, AbstractProtocol arg) {
		this.srcObject = src;
		this.dstObject = dst;
		this.arg = arg;
		this.res = null;
	}
	
	@Override
	public AbstractProtocol call() throws Exception {
		res = dstObject.call(arg);
		return null;
	}

	@Override
	public void onSuccess(AbstractProtocol result) {
		
	}

	@Override
	public void onFailure(Throwable t) {
		// TODO Auto-generated method stub
		
	}

}
