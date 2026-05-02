#!/usr/bin/env bash
set -euo pipefail

DEMO_TMP_DIR="${DEMO_TMP_DIR:-$(mktemp -d)}"
STATE_FILE="$DEMO_TMP_DIR/state.edn"
EXPORT_FILE="$DEMO_TMP_DIR/general-export.md"

mkdir -p "$DEMO_TMP_DIR"
rm -f "$STATE_FILE" "$EXPORT_FILE"
export PI_CHAT_STATE_FILE="$STATE_FILE"

run_chat() {
  printf '\n$ clojure -M:chat'
  printf ' %q' "$@"
  printf '\n'
  clojure -M:chat "$@"
}

printf 'Demo end-to-end de chat Markdown exportable\n'
printf 'Estado aislado: %s\n' "$STATE_FILE"

run_chat create-user andres
printf 'Sala personal de andres disponible: room:user:andres\n'
run_chat create-user zoe
printf 'Sala personal de zoe disponible: room:user:zoe\n'

run_chat create-room general
run_chat join general andres
run_chat join general zoe

run_chat send general andres 'Hola **equipo**' demo-andres-1
run_chat send general zoe $'- item uno\n- item dos' demo-zoe-1

run_chat show general andres
run_chat export general andres --output "$EXPORT_FILE"

grep -F '# General' "$EXPORT_FILE" >/dev/null
grep -F 'Hola **equipo**' "$EXPORT_FILE" >/dev/null
grep -F -- '- item uno' "$EXPORT_FILE" >/dev/null

printf '\nExportación verificada: %s\n' "$EXPORT_FILE"
printf '\nPrimeras líneas del Markdown exportado:\n'
sed -n '1,12p' "$EXPORT_FILE"
