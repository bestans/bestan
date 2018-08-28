package bestan.common.logic;

import com.google.protobuf.Message;

public class CommonCallback implements ICallback<Message>{
	protected IObject srcObject;
	protected IObject dstObject;
	protected Message arg;
	protected Message res;
	
	public CommonCallback(IObject src, IObject dst, Message arg) {
		this.srcObject = src;
		this.dstObject = dst;
		this.arg = arg;
		this.res = null;
	}
	
	@Override
	public Message call() throws Exception {
		res = dstObject.callbackExecute(arg);
		return null;
	}

	@Override
	public void onSuccess(Message result) {
		
	}

	@Override
	public void onFailure(Throwable t) {
		// TODO Auto-generated method stub
		
	}

}
