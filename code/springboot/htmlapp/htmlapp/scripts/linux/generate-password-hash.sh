#!/usr/bin/env bash
# vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab :

# -----------------------------------------------------------------------------
# Generador de hash de contraseña para la aplicación HTMLApp.
# -----------------------------------------------------------------------------
# Uso:
#   ./generate-password-hash.sh <contraseña>
# o sin argumentos para introducirla manualmente.
# -----------------------------------------------------------------------------

set -e

echo "=== Ejecutando GeneratePasswordHash ==="
mvn exec:java \
  -Dexec.mainClass="com.example.htmlapp.tools.GeneratePasswordHash" \
  -Dexec.args="$*"
