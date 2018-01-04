@echo off  
echo ** setting runtime variable  

echo 路径需要配置绝对路径  

set CurrentDir=%~dp0
set REPO_ROOT_DIR=%CurrentDir%..\..\
set GS_DIR=%REPO_ROOT_DIR%..\Server\gs\

REM _protoSrc 是你的proto文件目录的位置  
set _protoSrc=G:\my_work\tools\proto\cs.proto

REM protoExe 是用于从proto生成java的protoc.exe程序的位置  
set protoExe=G:\my_work\tools\protoc.exe 

REM java_out_file 存放生成的Java文件目录的位置  
set cpp_out_file=G:\my_work\tools\messages

%protoExe% --version

set SRC_DIR=G:\my_work\tools
set DST_DIR=G:\my_work\tools\messages
echo %SRC_DIR%cs.proto

protoc -I=%SRC_DIR% --cpp_out=%DST_DIR% %SRC_DIR%\extensions.proto
protoc -I=%SRC_DIR% --cpp_out=%DST_DIR% %SRC_DIR%\test.proto

REM dealExe 是添加预编译指令
REM set DEAL_EXE=%CurrentDir%AddPrecompile.exe
REM for /R "%cpp_out_file%" %%i in (*) do (
REM     if "%%~xi"  == ".cc" (
REM         %DEAL_EXE% %%i
REM     )
REM     if "%%~xi"  == ".h" (
REM         %DEAL_EXE% %%i
REM     )
REM )

echo Success
pause