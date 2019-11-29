@echo off  
set CurrentDir=%~dp0

echo %CurrentDir%

set protoExe=%CurrentDir%\proto_bin\protoc.exe
%protoExe% --version

set SRC_DIR=%CurrentDir%
set DST_DIR=%CurrentDir%\

%protoExe% -I=%SRC_DIR% --csharp_out=%DST_DIR% %SRC_DIR%\base.proto

echo Success
pause