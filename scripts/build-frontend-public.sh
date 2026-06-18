#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../frontend"
npm install --legacy-peer-deps --no-audit --no-fund
npm run build
printf '\nBuild público generado en frontend/dist/\n'
printf 'El frontend productivo usa API: https://soporteportales-api.dssm.cl/api\n'
