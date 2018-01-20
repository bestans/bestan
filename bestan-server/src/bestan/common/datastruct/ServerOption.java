package bestan.common.datastruct;

public class ServerOption {
	public enum SERVER_TYPE
	{
		INAVLID,
		HTTP_SERVER,
	}
	
	//服务器类型
	public SERVER_TYPE serverType = SERVER_TYPE.INAVLID;
	//服务器ip
	public String serverIP = "127.0.0.1";
	//服务器端口号
	public int serverPort = 8080;
	//工作线程数
	public int netThreadCount = 3;
}
