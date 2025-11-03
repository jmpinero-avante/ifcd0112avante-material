#!/usr/bin/env bash
# vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

# ============================================================
# Script de arranque para aplicación Spring Boot (macOS/Linux)
# ============================================================

# Salir si ocurre cualquier error
set -e

# Comprobamos que existe el pom.xml
if [ ! -f "pom.xml" ]; then
  echo "Error: este script debe ejecutarse desde el directorio raíz del proyecto (donde está pom.xml)"
  exit 1
fi

# Variables por defecto
PROFILE="dev"
PORT=8080
ARGS=""

# Leer parámetros opcionales
while [[ $# -gt 0 ]]; do
  case "$1" in
    --profile)
      PROFILE="$2"
      shift 2
      ;;
    --port)
      PORT="$2"
      shift 2
      ;;
    *)
      ARGS="$ARGS $1"
      shift
      ;;
  esac
done

echo "Iniciando aplicación Spring Boot..."
echo "Perfil: $PROFILE"
echo "Puerto: $PORT"
echo "-------------------------------------------"

# Comando de ejecución
mvn spring-boot:run \
  -Dspring-boot.run.profiles="$PROFILE" \
  -Dspring-boot.run.arguments="--server.port=$PORT $ARGS"