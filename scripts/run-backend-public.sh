#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../backend"
# Usa backend/src/main/resources/application-public.yml. No requiere export.
mvn spring-boot:run -Dspring-boot.run.profiles=public
