--1kb
local KB = 1024
--1兆
local MB = KB * KB

local config =
{
	clientName = "TestClient",
	
	bossGroupThreadCount = 1,
	
	workerGroupThreadCount = 1,
	
	serverIP = "127.0.0.1",
	
	serverPort = 1019,
}

return config