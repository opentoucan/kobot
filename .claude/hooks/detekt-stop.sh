#!/usr/bin/env bash
# Stop hook: run detekt when Kotlin sources have changed and send violations
# back to Claude (exit 2) so it fixes them before finishing the turn.
set -u

input=$(cat)
# Don't trap Claude in a loop: if it is already continuing because of this
# hook, let it stop. It gets one fix attempt per turn.
if printf '%s' "$input" | grep -q '"stop_hook_active":[[:space:]]*true'; then
  exit 0
fi

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

# Skip unless Kotlin or build-script changes exist since the branch left main.
# This includes changes committed during the turn, not just uncommitted ones.
base=$(git merge-base HEAD origin/main 2>/dev/null || git merge-base HEAD main 2>/dev/null || echo HEAD)
if git diff --quiet "$base" -- '*.kt' '*.kts' &&
  [ -z "$(git ls-files --others --exclude-standard -- '*.kt' '*.kts')" ]; then
  exit 0
fi

if ! command -v detekt >/dev/null 2>&1; then
  echo "detekt CLI not found on PATH; skipping lint check" >&2
  exit 0
fi

if ! out=$(detekt --config detekt.yaml --input ./src,build.gradle.kts 2>&1); then
  {
    echo "detekt failed. Fix these violations before finishing:"
    printf '%s\n' "$out" | grep -v '^WARNING:' | tail -n 40
  } >&2
  exit 2
fi
exit 0
