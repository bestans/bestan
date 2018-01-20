@echo off  
set CurrentDir=%~dp0

echo %CurrentDir%

set protoExe=%CurrentDir%\proto_bin\protoc.exe
%protoExe% --version

set SRC_DIR=%CurrentDir%
set DST_DIR=%CurrentDir%\..\

%protoExe% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\net_common.proto
%protoExe% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\net_base.proto

echo Success
pause