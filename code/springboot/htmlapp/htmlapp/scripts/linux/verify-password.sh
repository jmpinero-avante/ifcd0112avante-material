#!/usr/bin/env bash
# vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab :

# -----------------------------------------------------------------------------
# Verificador de contraseñas: compara una contraseña, salt y hash
# con los valores generados por PasswordService.
# -----------------------------------------------------------------------------
# Uso:
#   ./verify-password.sh --password <pwd> --salt <salt> --hash <hash>
# Si no se pasan argumentos, se pedirán interactivamente.
# -----------------------------------------------------------------------------

set -e

echo "=== Ejecutando VerifyPassword ==="
mvn exec:java \
  -Dexec.mainClass="com.example.htmlapp.tools.VerifyPassword" \
  -Dexec.args="$*"
