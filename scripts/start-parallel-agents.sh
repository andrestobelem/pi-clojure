#!/usr/bin/env bash
set -euo pipefail

PROJECT_OWNER="${PROJECT_OWNER:-andrestobelem}"
PROJECT_NUMBER="${PROJECT_NUMBER:-2}"
REPO="${REPO:-andrestobelem/pi-clojure}"
BASE_BRANCH="${BASE_BRANCH:-main}"
TMUX_SESSION="${TMUX_SESSION:-pi-parallel}"
WORKTREE_ROOT="${WORKTREE_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
WORKTREE_PREFIX="${WORKTREE_PREFIX:-pi-clojure}"
BRANCH_PREFIX="${BRANCH_PREFIX:-story}"
PI_COMMAND="${PI_COMMAND:-pi}"
DEFAULT_ISSUES="${DEFAULT_ISSUES:-}"
STATUS_VALUE="${STATUS_VALUE:-In Progress}"
FOCO_VALUE="${FOCO_VALUE:-Ahora}"
AGENT_PROMPT_EXTRA="${AGENT_PROMPT_EXTRA:-}"
SKIP_PROJECT_UPDATE="${SKIP_PROJECT_UPDATE:-false}"
SKIP_ISSUE_COMMENT="${SKIP_ISSUE_COMMENT:-false}"
DRY_RUN="${DRY_RUN:-false}"

PROJECT_ID="${PROJECT_ID:-}"
STATUS_FIELD_ID="${STATUS_FIELD_ID:-}"
STATUS_OPTION_ID="${STATUS_OPTION_ID:-}"
FOCO_FIELD_ID="${FOCO_FIELD_ID:-}"
FOCO_OPTION_ID="${FOCO_OPTION_ID:-}"

usage() {
  cat <<'USAGE'
Usage:
  scripts/start-parallel-agents.sh [options] <issue> [issue...]

Examples:
  scripts/start-parallel-agents.sh 19 3
  scripts/start-parallel-agents.sh --tmux-session pi-mvp --worktree-prefix chat 19 3
  DEFAULT_ISSUES="19 3" scripts/start-parallel-agents.sh

Options:
  --project-owner <owner>       GitHub Project owner.
  --project-number <number>     GitHub Project number.
  --repo <owner/name>           GitHub repository.
  --base-branch <branch>        Base branch for story branches.
  --tmux-session <name>         tmux session name.
  --worktree-root <path>        Parent directory for worktrees.
  --worktree-prefix <prefix>    Prefix for generated worktree names.
  --branch-prefix <prefix>      Prefix for generated branches.
  --pi-command <command>        Agent command to launch. Default: pi.
  --default-issues "1 2"        Issues used when no positional issues are passed.
  --status-value <name>         Project Status option. Default: In Progress.
  --foco-value <name>           Project Foco option. Default: Ahora.
  --prompt-extra <text>         Extra text appended to each agent prompt.
  --skip-project-update         Do not edit GitHub Project fields.
  --skip-issue-comment          Do not comment on issues.
  --dry-run                     Print planned worktrees/windows without creating them.
  -h, --help                    Show this help.

Environment variables mirror the long option names in uppercase:
  PROJECT_OWNER, PROJECT_NUMBER, REPO, BASE_BRANCH, TMUX_SESSION,
  WORKTREE_ROOT, WORKTREE_PREFIX, BRANCH_PREFIX, PI_COMMAND, DEFAULT_ISSUES,
  STATUS_VALUE, FOCO_VALUE, AGENT_PROMPT_EXTRA, SKIP_PROJECT_UPDATE,
  SKIP_ISSUE_COMMENT, DRY_RUN.

Advanced Project IDs can also be supplied to avoid discovery calls:
  PROJECT_ID, STATUS_FIELD_ID, STATUS_OPTION_ID, FOCO_FIELD_ID, FOCO_OPTION_ID.
USAGE
}

require_value() {
  local option="${1:-}" value="${2:-}"
  if [ -z "$value" ]; then
    echo "La opción $option requiere un valor." >&2
    exit 2
  fi
}

parse_args() {
  ISSUES=()
  while [ "$#" -gt 0 ]; do
    case "$1" in
      --project-owner) require_value "$1" "${2:-}"; PROJECT_OWNER="$2"; shift 2 ;;
      --project-number) require_value "$1" "${2:-}"; PROJECT_NUMBER="$2"; shift 2 ;;
      --repo) require_value "$1" "${2:-}"; REPO="$2"; shift 2 ;;
      --base-branch) require_value "$1" "${2:-}"; BASE_BRANCH="$2"; shift 2 ;;
      --tmux-session) require_value "$1" "${2:-}"; TMUX_SESSION="$2"; shift 2 ;;
      --worktree-root) require_value "$1" "${2:-}"; WORKTREE_ROOT="$2"; shift 2 ;;
      --worktree-prefix) require_value "$1" "${2:-}"; WORKTREE_PREFIX="$2"; shift 2 ;;
      --branch-prefix) require_value "$1" "${2:-}"; BRANCH_PREFIX="$2"; shift 2 ;;
      --pi-command) require_value "$1" "${2:-}"; PI_COMMAND="$2"; shift 2 ;;
      --default-issues) require_value "$1" "${2:-}"; DEFAULT_ISSUES="$2"; shift 2 ;;
      --status-value) require_value "$1" "${2:-}"; STATUS_VALUE="$2"; shift 2 ;;
      --foco-value) require_value "$1" "${2:-}"; FOCO_VALUE="$2"; shift 2 ;;
      --prompt-extra) require_value "$1" "${2:-}"; AGENT_PROMPT_EXTRA="$2"; shift 2 ;;
      --skip-project-update) SKIP_PROJECT_UPDATE=true; shift ;;
      --skip-issue-comment) SKIP_ISSUE_COMMENT=true; shift ;;
      --dry-run) DRY_RUN=true; shift ;;
      -h|--help) usage; exit 0 ;;
      --) shift; while [ "$#" -gt 0 ]; do ISSUES+=("$1"); shift; done ;;
      -*) echo "Opción desconocida: $1" >&2; usage >&2; exit 2 ;;
      *) ISSUES+=("$1"); shift ;;
    esac
  done

  if [ "${#ISSUES[@]}" -eq 0 ] && [ -n "$DEFAULT_ISSUES" ]; then
    # shellcheck disable=SC2206
    ISSUES=($DEFAULT_ISSUES)
  fi

  if [ "${#ISSUES[@]}" -eq 0 ]; then
    echo "Indicá al menos una issue o configurá DEFAULT_ISSUES." >&2
    usage >&2
    exit 2
  fi
}

run() {
  if [ "$DRY_RUN" = true ]; then
    printf '[dry-run] %s\n' "$*"
  else
    "$@"
  fi
}

slug_for_issue() {
  gh issue view "$1" --repo "$REPO" --json title --jq '.title' \
    | python3 -c 'import re, sys, unicodedata
text = sys.stdin.read().strip().lower()
text = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")
text = re.sub(r"[^a-z0-9]+", "-", text).strip("-")
text = re.sub(r"-+", "-", text)
print(text[:48].strip("-"))'
}

worktree_name_for_issue() {
  local issue="$1" slug="$2"
  echo "${WORKTREE_PREFIX}-${issue}-${slug}"
}

prompt_for_issue() {
  local issue="$1"
  cat <<PROMPT
Implementá la issue #${issue} con TDD/TDD Design en este worktree.

Reglas obligatorias:
- Respondé siempre en español.
- Antes de editar: revisá git status --short --branch y el contenido de la issue con gh issue view ${issue} --repo ${REPO}.
- Ciclo Red/Green/Refactor; escribí primero tests o ejemplos que fallen.
- Después de editar Clojure, corré clj-kondo --lint src test y clojure -M:test.
- Si editás Markdown, corré npx markdownlint-cli2 '**/*.md' '#node_modules'.
- Hacé commits atómicos con Conventional Commits cuando la suite esté verde.
- No edites archivos fuera de este worktree.
- Al terminar, dejá resumen, checks ejecutados y estado de git.
PROMPT
  if [ -n "$AGENT_PROMPT_EXTRA" ]; then
    printf '\nInstrucciones adicionales:\n%s\n' "$AGENT_PROMPT_EXTRA"
  fi
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

project_field_id_by_name() {
  gh project field-list "$PROJECT_NUMBER" --owner "$PROJECT_OWNER" --format json \
    --jq ".fields[] | select(.name==\"$1\") | .id"
}

project_option_id_by_name() {
  local field_name="$1" option_name="$2"
  gh project field-list "$PROJECT_NUMBER" --owner "$PROJECT_OWNER" --format json \
    --jq ".fields[] | select(.name==\"$field_name\") | .options[] | select(.name==\"$option_name\") | .id"
}

discover_project_ids() {
  if [ "$SKIP_PROJECT_UPDATE" = true ]; then
    return
  fi
  PROJECT_ID="${PROJECT_ID:-$(gh project view "$PROJECT_NUMBER" --owner "$PROJECT_OWNER" --format json --jq '.id')}"
  STATUS_FIELD_ID="${STATUS_FIELD_ID:-$(project_field_id_by_name Status)}"
  STATUS_OPTION_ID="${STATUS_OPTION_ID:-$(project_option_id_by_name Status "$STATUS_VALUE")}"
  FOCO_FIELD_ID="${FOCO_FIELD_ID:-$(project_field_id_by_name Foco)}"
  FOCO_OPTION_ID="${FOCO_OPTION_ID:-$(project_option_id_by_name Foco "$FOCO_VALUE")}"
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
  if [ "$SKIP_PROJECT_UPDATE" = true ]; then
    return
  fi
  item_id="$(project_item_id_for_issue "$issue")"
  if [ -z "$item_id" ]; then
    echo "No encontré la issue #$issue en el Project $PROJECT_NUMBER; omito actualización." >&2
    return
  fi
  run gh project item-edit --id "$item_id" --project-id "$PROJECT_ID" \
    --field-id "$STATUS_FIELD_ID" --single-select-option-id "$STATUS_OPTION_ID" >/dev/null
  run gh project item-edit --id "$item_id" --project-id "$PROJECT_ID" \
    --field-id "$FOCO_FIELD_ID" --single-select-option-id "$FOCO_OPTION_ID" >/dev/null
}

ensure_worktree() {
  local issue="$1" slug branch path
  slug="$(slug_for_issue "$issue")"
  branch="${BRANCH_PREFIX}/${issue}-${slug}"
  path="${WORKTREE_ROOT}/$(worktree_name_for_issue "$issue" "$slug")"

  if [ "$DRY_RUN" = true ]; then
    printf '%s|%s|%s\n' "$issue" "$branch" "$path"
    return
  fi

  gh issue develop "$issue" --repo "$REPO" --name "$branch" --base "$BASE_BRANCH" >/dev/null || true
  if ! git show-ref --verify --quiet "refs/heads/$branch"; then
    git fetch origin "$branch:$branch" --quiet || true
  fi

  if [ ! -e "$path/.git" ]; then
    git worktree add "$path" "$branch"
  fi

  mark_in_progress "$issue"
  if [ "$SKIP_ISSUE_COMMENT" != true ]; then
    gh issue comment "$issue" --repo "$REPO" \
      --body "Inicio/continuación de trabajo automatizado con agente Pi. Branch: \`$branch\`. Worktree: \`$path\`." >/dev/null || true
  fi

  printf '%s|%s|%s\n' "$issue" "$branch" "$path"
}

escape_double_quotes() {
  sed 's/"/\\"/g'
}

start_tmux() {
  local entries_file="$1"

  if [ "$DRY_RUN" = true ]; then
    echo "[dry-run] tmux session: $TMUX_SESSION"
    while IFS='|' read -r issue branch path; do
      echo "[dry-run] window issue-${issue}: branch=$branch path=$path command=$PI_COMMAND"
    done < "$entries_file"
    return
  fi

  if tmux has-session -t "$TMUX_SESSION" 2>/dev/null; then
    echo "Ya existe la sesión tmux '$TMUX_SESSION'. Attach con: tmux attach -t $TMUX_SESSION" >&2
    exit 1
  fi

  tmux new-session -d -s "$TMUX_SESSION" -n coordinator "cd '$PWD' && bash"

  while IFS='|' read -r issue _branch path; do
    local window_name="issue-${issue}"
    local prompt_file prompt_line
    prompt_file="$(mktemp -t "pi-issue-${issue}-prompt.XXXXXX")"
    prompt_for_issue "$issue" > "$prompt_file"
    prompt_line="$(tr '\n' ' ' < "$prompt_file" | escape_double_quotes)"
    tmux new-window -t "$TMUX_SESSION" -n "$window_name" \
      "cd '$path' && $PI_COMMAND \"$prompt_line\""
  done < "$entries_file"

  tmux select-window -t "$TMUX_SESSION:coordinator"
  echo "Agentes lanzados en tmux. Attach con: tmux attach -t $TMUX_SESSION"
}

main() {
  parse_args "$@"

  if ! command -v tmux >/dev/null; then
    echo "tmux no está instalado o no está en PATH." >&2
    exit 1
  fi

  if [ "$DRY_RUN" = true ]; then
    git status --short --branch
  else
    ensure_clean_main
  fi
  discover_project_ids

  local entries_file
  entries_file="$(mktemp -t pi-parallel-worktrees.XXXXXX)"
  for issue in "${ISSUES[@]}"; do
    ensure_worktree "$issue" >> "$entries_file"
  done

  start_tmux "$entries_file"
}

main "$@"
