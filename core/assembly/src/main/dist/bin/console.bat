@rem ***************************************************************************
@rem Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
@rem
@rem All rights reserved.
@rem
@rem Redistribution and use in source and binary forms, with or without
@rem modification, are permitted provided that the following conditions are met:
@rem
@rem - Redistributions of source code must retain the above copyright notice, this
@rem   list of conditions and the following disclaimer.
@rem - Redistributions in binary form must reproduce the above copyright notice,
@rem   this list of conditions and the following disclaimer in the documentation
@rem   and/or other materials provided with the distribution.
@rem - Neither the name of the Eclipse Foundation, Inc. nor the names of its
@rem   contributors may be used to endorse or promote products derived from this
@rem   software without specific prior written permission. 
@rem
@rem THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
@rem ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
@rem WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
@rem DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
@rem ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
@rem (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
@rem LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
@rem ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
@rem (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
@rem SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
@rem ***************************************************************************
@echo off

rem Set the lib dir relative to the batch file's directory
set LIB_DIR=%~dp0\..\lib
rem echo LIB_DIR = %LIB_DIR%

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=%1
if ""%1""=="""" goto setupArgsEnd
shift
:setupArgs
if ""%1""=="""" goto setupArgsEnd
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setupArgs

:setupArgsEnd

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto javaHome

:noJavaHome
set JAVA=java
goto javaHomeEnd

:javaHome
set JAVA=%JAVA_HOME%\bin\java

:javaHomeEnd

:checkJdk14
"%JAVA%" -version 2>&1 | findstr "1.4" >NUL
IF ERRORLEVEL 1 goto checkJdk15
echo Java 5 or newer required to run the console
goto end

:checkJdk15
"%JAVA%" -version 2>&1 | findstr "1.5" >NUL
IF ERRORLEVEL 1 goto java6
rem use java.ext.dirs hack
rem echo Using java.ext.dirs to set classpath
"%JAVA%" -Djava.ext.dirs="%LIB_DIR%" org.openrdf.console.Console %CMD_LINE_ARGS%
goto end

:java6
rem use java 6 wildcard feature
rem echo Using wildcard to set classpath
"%JAVA%" -cp "%LIB_DIR%\*" org.openrdf.console.Console %CMD_LINE_ARGS%
goto end

:end
