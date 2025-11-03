#!/usr/bin/env zsh
# vim: set tabstop=2 softtabstop=2 shiftwidth=2 noexpandtab :


set -euo pipefail

echo "=== Ejecutando GenerateUserInsert ==="

typeset SCRIPT FOLDER

SCRIPT=${(%):-'%x'}
SCRIPT=${SCRIPT:a}
FOLDER=${SCRIPT:h:h:h}

cd "${FOLDER}"

mvn exec:java \
	-Dexec.mainClass="com.example.htmlapp.tools.GenerateUserInsert" \
	-Dexec.args="${(j. .)${(qq)@}}"
