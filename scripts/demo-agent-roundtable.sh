#!/usr/bin/env bash
set -euo pipefail

DEMO_TMP_DIR="${DEMO_TMP_DIR:-.demo}"
STATE_FILE="$DEMO_TMP_DIR/agent-roundtable.edn"
EXPORT_FILE="$DEMO_TMP_DIR/agent-roundtable-demo.md"
PROMPTS_DIR="$DEMO_TMP_DIR/prompts"

mkdir -p "$DEMO_TMP_DIR" "$PROMPTS_DIR"
rm -f "$STATE_FILE" "$EXPORT_FILE"
export PI_CHAT_STATE_FILE="$STATE_FILE"

run_chat() {
  printf '\n$ clojure -M:chat'
  printf ' %q' "$@"
  printf '\n'
  clojure -M:chat "$@"
}

write_prompt() {
  local handle="$1" personality="$2" description="$3"
  cat > "$PROMPTS_DIR/$handle.md" <<PROMPT
Usá la CLI real de pi-clojure para colaborar en la sala roundtable.

Handle: $handle
Personalidad: $personality
Sesgo: $description

Reglas:
- Antes de responder, leé la sala con:
  clojure -M:chat show roundtable $handle
- Para hablar, enviá mensajes con:
  clojure -M:chat send roundtable $handle "<markdown>" "<txn-id>"
- No tenés un rol fijo: podés actuar como producto, dominio, UX, seguridad,
  tester, backlog gardener o implementador si la conversación lo pide.
- Si encontrás fricción usando la CLI, registrala como hallazgo.
- Si proponés una mejora, incluí criterios y primer test rojo sugerido.
- En esta demo no edites código ni crees issues automáticamente.
PROMPT
}

send_roundtable() {
  local handle="$1" txn="$2" body="$3"
  run_chat send roundtable "$handle" "$body" "$txn"
}

printf 'Dogfood agent roundtable\n'
printf 'Estado aislado: %s\n' "$STATE_FILE"
printf 'Personalidades: pragmatica, esceptica, narradora\n'

write_prompt pragmatica "Pragmática" "busca el slice mínimo ejecutable y próximos pasos concretos"
write_prompt esceptica "Escéptica" "busca bugs, riesgos, edge cases y tests rojos"
write_prompt narradora "Narradora" "cuida claridad, demo y experiencia de uso"

run_chat create-user pragmatica
run_chat create-user esceptica
run_chat create-user narradora
run_chat create-room roundtable
run_chat join roundtable pragmatica
run_chat join roundtable esceptica
run_chat join roundtable narradora

send_roundtable pragmatica pragmatica-001 $'### Hallazgo Pragmática\n\nPodemos usar el chat como pizarra compartida si el primer corte serializa turnos y evita resolver concurrencia todavía.\n\n### Próximo paso\n\nCrear un script que prepare estado aislado, usuarios con personalidades y exporte la conversación.'

run_chat show roundtable esceptica
send_roundtable esceptica esceptica-001 $'### Hallazgo Escéptica\n\nEl state file EDN compartido puede corromperse si varios agentes escriben a la vez. La demo debe documentar ese riesgo o serializar turnos.\n\n### Primer test rojo sugerido\n\nDado un directorio temporal, el script de dogfood crea estado aislado, envía mensajes y exporta un transcript con una story candidata.'

run_chat show roundtable narradora
send_roundtable narradora narradora-001 $'### Hallazgo Narradora\n\nLa demo cuenta mejor la historia si las personalidades no son roles fijos: cada agente puede descubrir UX, dominio o testing mientras usa el producto.\n\n### Story candidata\n\nComo mantenedor, quiero una demo dogfood donde agentes usen el chat por CLI para descubrir backlog colaborativamente.\n\nCriterios:\n\n- crea estado aislado;\n- crea agentes con personalidades;\n- usa la CLI real para conversar;\n- exporta el transcript;\n- no crea issues automáticamente.'

run_chat export roundtable pragmatica --output "$EXPORT_FILE"

grep -F '# Roundtable' "$EXPORT_FILE" >/dev/null
grep -F 'Pragmática' "$EXPORT_FILE" >/dev/null
grep -F 'Escéptica' "$EXPORT_FILE" >/dev/null
grep -F 'Narradora' "$EXPORT_FILE" >/dev/null
grep -F '### Story candidata' "$EXPORT_FILE" >/dev/null
grep -F 'Primer test rojo sugerido' "$EXPORT_FILE" >/dev/null

printf '\nPrompts generados en: %s\n' "$PROMPTS_DIR"
printf 'Transcript exportado: %s\n' "$EXPORT_FILE"
printf '\nPara lanzar agentes pi manualmente:\n'
printf '  PI_CHAT_STATE_FILE=%q pi "$(cat %q)"\n' "$STATE_FILE" "$PROMPTS_DIR/pragmatica.md"
printf '  PI_CHAT_STATE_FILE=%q pi "$(cat %q)"\n' "$STATE_FILE" "$PROMPTS_DIR/esceptica.md"
printf '  PI_CHAT_STATE_FILE=%q pi "$(cat %q)"\n' "$STATE_FILE" "$PROMPTS_DIR/narradora.md"
