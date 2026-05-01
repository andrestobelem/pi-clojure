#!/usr/bin/env bash
set -euo pipefail

PROJECT_OWNER="${PROJECT_OWNER:-andrestobelem}"
PROJECT_NUMBER="${PROJECT_NUMBER:-2}"
REPO="${REPO:-andrestobelem/pi-clojure}"
BASE_BRANCH="${BASE_BRANCH:-main}"
TMUX_SESSION="${TMUX_SESSION:-pi-parallel}"
WORKTREE_ROOT="${WORKTREE_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"

PROJECT_ID="PVT_kwHOAEKsO84BWWrE"
STATUS_FIELD_ID="PVTSSF_lAHOAEKsO84BWWrEzhRrst4"
STATUS_IN_PROGRESS_ID="47fc9ee4"
FOCO_FIELD_ID="PVTSSF_lAHOAEKsO84BWWrEzhRr-bc"
FOCO_AHORA_ID="529924ca"

usage() {
  cat <<'USAGE'
Usage:
  scripts/start-parallel-agents.sh [issue...]

Defaults to issues 18 and 17.

Environment:
  PROJECT_OWNER   GitHub project owner. Default: andrestobelem
  PROJECT_NUMBER  GitHub project number. Default: 2
  REPO            GitHub repository. Default: andrestobelem/pi-clojure
  BASE_BRANCH     Base branch for story branches. Default: main
  TMUX_SESSION    tmux session name. Default: pi-parallel
  WORKTREE_ROOT   Parent directory for worktrees. Default: repo parent
USAGE
}

slug_for_issue() {
  case "$1" in
    18) echo "export-room-markdown" ;;
    17) echo "list-accessible-rooms" ;;
    *) gh issue view "$1" --repo "$REPO" --json title --jq '.title' \
      | tr '[:upper:]' '[:lower:]' \
      | iconv -f utf-8 -t ascii//TRANSLIT 2>/dev/null \
      | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//; s/-+/-/g' \
      | cut -c1-48 ;;
  esac
}

worktree_name_for_issue() {
  case "$1" in
    18) echo "pi-clojure-18-export" ;;
    17) echo "pi-clojure-17-list-rooms" ;;
    *) echo "pi-clojure-$1-$(slug_for_issue "$1")" ;;
  esac
}

prompt_for_issue() {
  local issue="$1"
  cat <<PROMPT
Implementá la issue #${issue} con TDD/TDD Design en este worktree.

Reglas obligatorias:
- Respondé siempre en español.
- Antes de editar: revisá git status --short --branch y el contenido de la issue con gh issue view ${issue}.
- Ciclo Red/Green/Refactor; escribí primero tests o ejemplos que fallen.
- Después de editar Clojure, corré clj-kondo --lint src test y clojure -M:test.
- Si editás Markdown, corré npx markdownlint-cli2 '**/*.md' '#node_modules'.
- Hacé commits atómicos con Conventional Commits cuando la suite esté verde.
- No edites archivos fuera de este worktree.
- Al terminar, dejá resumen, checks ejecutados y estado de git.
PROMPT
}

ensure_clean_main() {
  git fetch origin "$BASE_BRANCH" --quiet
  local status
  status="$(git status --short --branch)"
  printf '%s\n' "$status"
  if printf '%s\n' "$status" | grep -q '^UU '; then
    echo "Hay conflictos sin resolver; abortando." >&2
    exit 1
  fi
  if [ -n "$(git status --porcelain)" ]; then
    echo "El worktree actual tiene cambios pendientes; abortando." >&2
    exit 1
  fi
}

project_item_id_for_issue() {
  local issue="$1"
  gh project item-list "$PROJECT_NUMBER" --owner "$PROJECT_OWNER" --format json --limit 100 \
    | python3 -c 'import json,sys
issue=int(sys.argv[1])
for item in json.load(sys.stdin)["items"]:
    if item.get("content", {}).get("number") == issue:
        print(item["id"])
        break
' "$issue"
}

mark_in_progress() {
  local issue="$1" item_id
  item_id="$(project_item_id_for_issue "$issue")"
  if [ -z "$item_id" ]; then
    echo "No encontré la issue #$issue en el Project $PROJECT_NUMBER; omito actualización." >&2
    return
  fi
  gh project item-edit --id "$item_id" --project-id "$PROJECT_ID" \
    --field-id "$STATUS_FIELD_ID" --single-select-option-id "$STATUS_IN_PROGRESS_ID" >/dev/null
  gh project item-edit --id "$item_id" --project-id "$PROJECT_ID" \
    --field-id "$FOCO_FIELD_ID" --single-select-option-id "$FOCO_AHORA_ID" >/dev/null
}

ensure_worktree() {
  local issue="$1" slug branch path
  slug="$(slug_for_issue "$issue")"
  branch="story/${issue}-${slug}"
  path="${WORKTREE_ROOT}/$(worktree_name_for_issue "$issue")"

  gh issue develop "$issue" --repo "$REPO" --name "$branch" --base "$BASE_BRANCH" >/dev/null || true
  if ! git show-ref --verify --quiet "refs/heads/$branch"; then
    git fetch origin "$branch:$branch" --quiet || true
  fi

  if [ ! -e "$path/.git" ]; then
    git worktree add "$path" "$branch"
  fi

  mark_in_progress "$issue"
  gh issue comment "$issue" --repo "$REPO" \
    --body "Inicio/continuación de trabajo automatizado con agente Pi. Branch: \`$branch\`. Worktree: \`$path\`." >/dev/null || true

  printf '%s|%s|%s\n' "$issue" "$branch" "$path"
}

start_tmux() {
  local entries_file="$1"

  if tmux has-session -t "$TMUX_SESSION" 2>/dev/null; then
    echo "Ya existe la sesión tmux '$TMUX_SESSION'. Attach con: tmux attach -t $TMUX_SESSION" >&2
    exit 1
  fi

  tmux new-session -d -s "$TMUX_SESSION" -n coordinator "cd '$PWD' && bash"

  while IFS='|' read -r issue _branch path; do
    local window_name="issue-${issue}"
    local prompt_file
    prompt_file="$(mktemp -t "pi-issue-${issue}-prompt.XXXXXX")"
    prompt_for_issue "$issue" > "$prompt_file"
    tmux new-window -t "$TMUX_SESSION" -n "$window_name" \
      "cd '$path' && pi \"$(tr '\n' ' ' < "$prompt_file" | sed 's/"/\\"/g')\""
  done < "$entries_file"

  tmux select-window -t "$TMUX_SESSION:coordinator"
  echo "Agentes lanzados en tmux. Attach con: tmux attach -t $TMUX_SESSION"
}

main() {
  if [ "${1:-}" = "--help" ] || [ "${1:-}" = "-h" ]; then
    usage
    exit 0
  fi

  if ! command -v tmux >/dev/null; then
    echo "tmux no está instalado o no está en PATH." >&2
    exit 1
  fi

  ensure_clean_main

  local issues=("$@")
  if [ "${#issues[@]}" -eq 0 ]; then
    issues=(18 17)
  fi

  local entries_file
  entries_file="$(mktemp -t pi-parallel-worktrees.XXXXXX)"
  for issue in "${issues[@]}"; do
    ensure_worktree "$issue" >> "$entries_file"
  done

  start_tmux "$entries_file"
}

main "$@"
