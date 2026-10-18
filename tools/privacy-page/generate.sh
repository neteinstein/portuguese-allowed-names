#!/usr/bin/env bash
# Regenerates the website's privacy page (webApp/src/wasmJsMain/resources/privacy.html, served
# at <site>/privacy.html) from PRIVACY_POLICY.md, so the app, the Play Store listing and the web
# page all show the same policy. Needs pandoc. Run from anywhere:
#   ./tools/privacy-page/generate.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEMPLATE="$ROOT/tools/privacy-page/template.html"
OUTPUT="$ROOT/webApp/src/wasmJsMain/resources/privacy.html"

BODY="$(pandoc -f gfm -t html --wrap=none "$ROOT/PRIVACY_POLICY.md")"

# Split the template at its {{BODY}} line rather than substituting with sed: the policy's HTML
# is full of characters sed would treat as special.
{
    sed '/{{BODY}}/,$d' "$TEMPLATE"
    printf '%s\n' "$BODY"
    sed '1,/{{BODY}}/d' "$TEMPLATE"
} > "$OUTPUT"

echo "Wrote $OUTPUT"
