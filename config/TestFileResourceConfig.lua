--1kb
local KB = 1024
--1兆
local MB = KB * KB

local config =
{
	resourceDir = "E:/bestan/config/download/",
	versionFile = "version.txt",
	
	--旧资源链接过期时间（秒）
	oldConnectionExpiredTime = 120,
	--资源管理器tick间隔时间（毫秒）
	tickInterval = 100,
	
}

return config