@echo off  
set CurrentDir=%~dp0

echo %CurrentDir%

set protoExe=%CurrentDir%\proto_bin\protoc.exe
set grpcExe=%CurrentDir%\protoc-gen-grpc-java-1.14.0-windows-x86_64.exe
::enumExe生成消息枚举索引，参数是枚举文件名
set enumExe=%CurrentDir%\protoc_message_enum.exe
%protoExe% --version

set SRC_DIR=%CurrentDir%
set DST_DIR=%CurrentDir%\..\

set FILE_DIR=%DST_DIR%bestan\common\protobuf\
set DEST_FILE_DIR=E:\bestan\bestan-common\src\main\java\bestan\common\protobuf

:%protoExe% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\net_common.proto
:%protoExe% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\proto.proto
%protoExe% --plugin=protoc-gen-enum=%enumExe% --enum_out=%DST_DIR% --enum_opt=MessageEnum -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\proto.proto
:%protoExe% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\net_base.proto
:%protoExe% --plugin=protoc-gen-grpc-java=%enumExe% --grpc-java_out=%DST_DIR% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\helloworld.proto
:%protoExe% --plugin=protoc-gen-grpc-java=%enumExe% --grpc-java_out=%DST_DIR% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\proto.proto

:%protoExe% -I=%SRC_DIR% --csharp_out=%DST_DIR% %SRC_DIR%\net_common.proto
:%protoExe% -I=%SRC_DIR% --csharp_out=%DST_DIR% %SRC_DIR%\net_base.proto

echo %FILE_DIR%Proto.java
echo %DEST_FILE_DIR%

copy %FILE_DIR%Proto.java %DEST_FILE_DIR%
copy %FILE_DIR%MessageEnum.java %DEST_FILE_DIR%

echo Success
pause