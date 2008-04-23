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

set LIB_DIR=%CD%\..\lib
rem echo LIB_DIR = %LIB_DIR%

:checkJdk14
"%JAVA_HOME%\bin\java" -version 2>&1 | findstr "1.4" >NUL
IF ERRORLEVEL 1 goto checkJdk15
echo Java 5 or newer required to run the console
goto end

:checkJdk15
"%JAVA_HOME%\bin\java" -version 2>&1 | findstr "1.5" >NUL
IF ERRORLEVEL 1 goto java6
rem use java.ext.dirs hack
rem echo Using java.ext.dirs to set classpath
"%JAVA_HOME%\bin\java" -Djava.ext.dirs="%LIB_DIR%" org.openrdf.console.Console %CMD_LINE_ARGS%
goto end

:java6
rem use java 6 wildcard feature
rem echo Using wildcard to set classpath
"%JAVA_HOME%\bin\java" -cp "%LIB_DIR%\*" org.openrdf.console.Console %CMD_LINE_ARGS%
goto end

:end
