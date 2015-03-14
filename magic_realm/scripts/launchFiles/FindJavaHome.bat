@echo off
cls
SET JavaVersion=
SET JAVA_HOME=
FOR /F "tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment" /v CurrentVersion') DO SET JavaVersion=%%B
if "%JavaVersion%" EQU "" goto :NOJAVA
FOR /F "tokens=2*" %%A IN ('REG QUERY "HKLM\Software\JavaSoft\Java Runtime Environment\%JavaVersion%" /v JavaHome') DO SET JAVA_HOME=%%B\bin
if "%JAVA_HOME%" EQU "" goto :BADJAVA
echo Current version of java = %JavaVersion%
echo Current java path = %JAVA_HOME%
echo .
echo ---------------------------------------
echo -- JAVA_HOME setup successful!
echo ---------------------------------------
goto :DONE

:NOJAVA
echo ****************************************
echo ** Java does not appear to be installed.
echo ****************************************
pause
goto :DONE

:BADJAVA
echo **************************************************
echo ** Java does not appear to be installed correctly.
echo **************************************************
pause
goto :DONE

:DONE