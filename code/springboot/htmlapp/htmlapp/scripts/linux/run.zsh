#!/usr/bin/env zsh
# vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab textwidth=80 :

# ============================================================
# Script de arranque para aplicación Spring Boot (macOS/Linux)
# ============================================================

# Salir si ocurre cualquier error
set -euo pipefail

# Vamos al directorio del pom
typeset SCRIPT FOLDER

SCRIPT=${(%):-'%x'}
SCRIPT=${SCRIPT:a}
FOLDER=${SCRIPT:h:h:h}

cd "${FOLDER}"

# Comprobamos que existe el pom.xml
if [ ! -f "pom.xml" ]; then
	echo "Error: este script debe ejecutarse desde el directorio raíz del proyecto (donde está pom.xml)"
	exit 1
fi

# Variables por defecto
PROFILE="dev"
PORT=8080

typeset -a theArgs=(${@})
typeset -a mvnArgs=()

typeset -i len=${#theArgs}
typeset -i index=1
typeset -i nextindex

typeset anarg nextarg

while (( index <= len )); do
	anarg=${theArgs[${index}]}

	if (( index > len - 1 )); then
		mvnArgs+=( ${anarg} )
	else
		nextindex=$(( index + 1 ))
		nextarg=${theArgs[${nextindex}]}

		case "${anarg}"; in
			--profile)
				PROFILE=${nextarg}
				(( index++ ))
				;;
			--port)
				PORT=${nextarg}
				(( index++ ))
				;;
			*)
				mvnArgs+=( ${anarg} )
				;;
		esac
	fi

	(( index++ ))
done


echo "Iniciando aplicación Spring Boot..."
echo "Perfil: $PROFILE"
echo "Puerto: $PORT"
echo "-------------------------------------------"

# Comando de ejecución
mvn spring-boot:run \
	-Dspring-boot.run.profiles="$PROFILE" \
	-Dspring-boot.run.arguments="--server.port=$PORT ${(j. .)${(qq@)mvnArgs}}"