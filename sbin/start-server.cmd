@echo off
REM  
REM  Author hans
REM 

setlocal
chcp 65001
   
rem ----- Execute The Requested Command ---------------------------------------
         
rem Guess APP_HOME if not defined
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
call "%APP_HOME%\conf\server-env.cmd"

if not "%JAVA_HOME%" == "" goto okJDKHome
if exist "%JAVA_HOME%\bin\java" goto okJDKHome
rem if not /i "%PROCESSOR_ARCHITECTURE%"=="AMD64" goto JDK32
set JAVA_HOME=%APP_HOME%\bin\jdk\win\jre
rem :JDK32
rem set JAVA_HOME=%APP_HOME%\bin\jdk\win\jre32
:okJDKHome
:execCmd
set _EXECJAVA=%JAVA_HOME%\bin\java

set JAVAMAIN=com.sobey.jcg.sobeyhive.install.Installer 


set CMD=%1
if "%CMD%" == "" set CMD=start

:execInsCmd
shift /1
echo on
"%_EXECJAVA%" -Xmx512m "%DEBUG%" -Dfile.encoding=UTF-8 -cp "%CLASSPATH%" %JAVAMAIN% -%CMD%  %1 %2 %3 %4 %5 %6 %7 
set RES_VAL=%errorlevel%
goto end


endlocal

:end
exit %RES_VAL%