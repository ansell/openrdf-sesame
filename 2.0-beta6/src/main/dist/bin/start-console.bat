@echo off

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart

set SESAME2_HOME=%0\..\..
cd %SESAME2_HOME%\lib
java -jar openrdf-console-2.0-beta6.jar %CMD_LINE_ARGS%
cd ..

