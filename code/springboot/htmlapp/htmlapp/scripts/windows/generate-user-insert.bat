@echo off
REM vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab :

REM -----------------------------------------------------------------------------
REM Verificador de contraseñas: compara una contraseña, salt y hash
REM con los valores generados por PasswordService.
REM -----------------------------------------------------------------------------
REM Uso:
REM   verify-password.bat --password <pwd> --salt <salt> --hash <hash>
REM Si no se pasan argumentos, se pedirán interactivamente.
REM -----------------------------------------------------------------------------

echo === Ejecutando VerifyPassword ===
mvn exec:java -Dexec.mainClass="com.example.htmlapp.tools.GenerateUserInsert" -Dexec.args="%*"
