#!/usr/bin/env zsh
# vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab :

# -----------------------------------------------------------------------------
# Generador de hash de contraseña para la aplicación HTMLApp.
# -----------------------------------------------------------------------------
# Uso:
#   ./generate-password-hash.sh <contraseña>
# o sin argumentos para introducirla manualmente.
# -----------------------------------------------------------------------------

set -euo pipefail

echo "=== Ejecutando GeneratePasswordHash ==="

typeset SCRIPT FOLDER

SCRIPT=${(%):-'%x'}
SCRIPT=${SCRIPT:a}
FOLDER=${SCRIPT:h:h:h}

cd "${FOLDER}"

mvn exec:java \
  -Dexec.mainClass="com.example.htmlapp.tools.GeneratePasswordHash" \
  -Dexec.args="${(j. .)${(qq)@}}"
