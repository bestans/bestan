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

:protoExe% -I=%SRC_DIR% --cpp_out=%DST_DIR% %SRC_DIR%\template_base.proto
%protoExe% -I=%SRC_DIR% --csharp_out=%DST_DIR% %SRC_DIR%\template_base.proto
%protoExe% -I=%SRC_DIR% --python_out=%DST_DIR% %SRC_DIR%\template_base.proto

echo %DEST_FILE_DIR%

:copy %DST_DIR%template_base.pb.h %DEST_FILE_DIR%
:copy %DST_DIR%template_base.pb.cc %DEST_FILE_DIR%
:copy %DST_DIR%TemplateBase.cs E:\dotnet\tools\test_frame
copy %DST_DIR%template_base_pb2.py E:\tools\csharp-tools\excel-configure\PythonParse

echo Success
pause