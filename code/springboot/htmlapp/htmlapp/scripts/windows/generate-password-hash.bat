@echo off
REM vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab :

REM -----------------------------------------------------------------------------
REM Generador de hash de contraseña para la aplicación HTMLApp.
REM -----------------------------------------------------------------------------
REM Uso:
REM   generate-password-hash.bat <contraseña>
REM o sin argumentos para introducirla manualmente.
REM -----------------------------------------------------------------------------

echo === Ejecutando GeneratePasswordHash ===
mvn exec:java -Dexec.mainClass="com.example.htmlapp.tools.GeneratePasswordHash" -Dexec.args="%*"