@echo off
REM  
REM  Author hans
REM 

set "CURRENT_DIR=%cd%"
if not "%APP_HOME%" == "" goto gotHome
set "APP_HOME=%CURRENT_DIR%"
if exist "%APP_HOME%\conf\server-env.cmd" goto okHome
cd ..
set "APP_HOME=%cd%"
cd "%CURRENT_DIR%"
:gotHome
if exist "%APP_HOME%\conf\server-env.cmd" goto okHome
echo The APP_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

set APPCFGDIR=%APP_HOME%\conf

REM add the conf dir to classpath
set CLASSPATH=%APPCFGDIR%

REM make it work in the release
SET CLASSPATH=%APP_HOME%\*;%APP_HOME%\lib\*;%APP_HOME%\lib\common\*;%APP_HOME%\lib\rdbms\*;%APP_HOME%\lib\jetty\*;%APP_HOME%\lib\jetty\websocket\*;%CLASSPATH%

REM make it work for developers
REM SET CLASSPATH=%CLASSPATH%;%APP_HOME%\ebin
set DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
set DEBUG="-Da=a"
