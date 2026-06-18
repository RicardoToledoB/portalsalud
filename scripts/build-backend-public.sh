#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../backend"
mvn clean package -DskipTests
printf '\nJAR generado en backend/target/soporte-portales-salud-0.0.1-SNAPSHOT.jar\n'
printf 'Ejecutar en servidor con: java -jar target/soporte-portales-salud-0.0.1-SNAPSHOT.jar --spring.profiles.active=public\n'
