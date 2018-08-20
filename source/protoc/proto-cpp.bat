@echo off  
set CurrentDir=%~dp0

echo %CurrentDir%

set protoExe=%CurrentDir%\proto_bin\protoc.exe
set grpcExe=%CurrentDir%\protoc-gen-grpc-java-1.14.0-windows-x86_64.exe
%protoExe% --version

set SRC_DIR=%CurrentDir%
set DST_DIR=%CurrentDir%\..\

:%protoExe% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\net_common.proto
%protoExe% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\proto.proto
:%protoExe% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\net_base.proto
:%protoExe% --plugin=protoc-gen-grpc-java=%grpcExe% --grpc-java_out=%DST_DIR% -I=%SRC_DIR% --java_out=%DST_DIR% %SRC_DIR%\helloworld.proto

%protoExe% -I=%SRC_DIR% --csharp_out=%DST_DIR% %SRC_DIR%\net_common.proto
%protoExe% -I=%SRC_DIR% --csharp_out=%DST_DIR% %SRC_DIR%\net_base.proto

echo Success
pause