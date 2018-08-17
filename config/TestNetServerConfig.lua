--1kb
local KB=1024
--1兆
local MB=KB*KB

local config=
{
	serverName = "TestServer",
	bossGroupThreadCount = 1,
	workerGroupThreadCount = 1,
	serverIP = "127.0.0.1",
	serverPort = 1019,
	--serverchanel(用来监听和接受连接)的接收缓冲区大小
	optionRcvbuf = 64 * KB,
	--serverchanel(用来监听和接受连接)的发送缓冲区大小
	optionSndbuf = 128 * KB,
	--clientchannel（每一个建立连接）的接收缓冲区大小
	childOptionRcvbuf = 16 * KB,
	--clientchannel（每一个建立连接）的发送缓冲区大小
	childOptionSndbuf = 32 * KB,
}

return config