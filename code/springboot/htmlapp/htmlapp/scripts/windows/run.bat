@echo off
REM ============================================================
REM Script de arranque para aplicación Spring Boot (Windows)
REM ============================================================

if not exist pom.xml (
  echo [ERROR] Este script debe ejecutarse desde el directorio raíz del proyecto.
  exit /b 1
)

set PROFILE=dev
set PORT=8080
set ARGS=

:loop
if "%~1"=="" goto run
if "%~1"=="--profile" (
  set PROFILE=%~2
  shift
  shift
  goto loop
)
if "%~1"=="--port" (
  set PORT=%~2
  shift
  shift
  goto loop
)
set ARGS=%ARGS% %~1
shift
goto loop

:run
echo Iniciando aplicación Spring Boot...
echo Perfil: %PROFILE%
echo Puerto: %PORT%
echo -------------------------------------------

call mvn spring-boot:run -Dspring-boot.run.profiles=%PROFILE% -Dspring-boot.run.arguments="--server.port=%PORT% %ARGS%"