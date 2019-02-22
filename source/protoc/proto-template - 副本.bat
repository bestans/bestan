@echo off  
set CurrentDir=%~dp0

echo %CurrentDir%

set protoExe=%CurrentDir%\proto_bin\protoc.exe
set grpcExe=%CurrentDir%\protoc-gen-grpc-java-1.14.0-windows-x86_64.exe
::enumExe生成消息枚举索引，参数是枚举文件名
set enumExe=%CurrentDir%\protoc_message_enum.exe
%protoExe% --version

set SRC_DIR=%CurrentDir%
set DST_DIR=%CurrentDir%\

set FILE_DIR=%DST_DIR%bestan\common\protobuf\
set DEST_FILE_DIR=E:\tools\cpp-tools\protobuf-desc

%protoExe% -I=%SRC_DIR% --python_out=%DST_DIR% %SRC_DIR%\addressbook.proto

echo %DEST_FILE_DIR%


echo Success
pause