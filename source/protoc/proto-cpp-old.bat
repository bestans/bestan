@echo off  
echo ** setting runtime variable  

echo ·����Ҫ���þ���·��  

set CurrentDir=%~dp0
set REPO_ROOT_DIR=%CurrentDir%..\..\
set GS_DIR=%REPO_ROOT_DIR%..\Server\gs\

REM _protoSrc �����proto�ļ�Ŀ¼��λ��  
set _protoSrc=G:\my_work\tools\proto\cs.proto

REM protoExe �����ڴ�proto����java��protoc.exe�����λ��  
set protoExe=G:\my_work\tools\protoc.exe 

REM java_out_file ������ɵ�Java�ļ�Ŀ¼��λ��  
set cpp_out_file=G:\my_work\tools\messages

%protoExe% --version

set SRC_DIR=G:\my_work\tools
set DST_DIR=G:\my_work\tools\messages
echo %SRC_DIR%cs.proto

protoc -I=%SRC_DIR% --cpp_out=%DST_DIR% %SRC_DIR%\extensions.proto
protoc -I=%SRC_DIR% --cpp_out=%DST_DIR% %SRC_DIR%\test.proto

REM dealExe �����Ԥ����ָ��
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