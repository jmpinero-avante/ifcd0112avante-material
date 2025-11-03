#!/usr/bin/env zsh
# vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab :

# -----------------------------------------------------------------------------
# Verificador de contraseñas: compara una contraseña, salt y hash
# con los valores generados por PasswordService.
# -----------------------------------------------------------------------------
# Uso:
#   ./verify-password.sh --password <pwd> --salt <salt> --hash <hash>
# Si no se pasan argumentos, se pedirán interactivamente.
# -----------------------------------------------------------------------------

set -euo pipefail

echo "=== Ejecutando VerifyPassword ==="

typeset SCRIPT FOLDER

SCRIPT=${(%):-'%x'}
SCRIPT=${SCRIPT:a}
FOLDER=${SCRIPT:h:h:h}

cd "${FOLDER}"

mvn exec:java \
  -Dexec.mainClass="com.example.htmlapp.tools.VerifyPassword" \
  -Dexec.args="${(j. .)${(qq)@}}"
