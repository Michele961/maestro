#!/usr/bin/env bash
# Crea il repository privato su GitHub, fa il push e configura main.
# Uso (dalla cartella del repo, con gh autenticato): ./scripts/setup-github.sh [nome-repo]
set -euo pipefail

REPO="${1:-maestro}"
OWNER="$(gh api user --jq .login)"
FULL="$OWNER/$REPO"

if gh repo view "$FULL" >/dev/null 2>&1; then
  echo "→ $FULL esiste già: collego il remote e faccio il push di main"
  git remote get-url origin >/dev/null 2>&1 || git remote add origin "https://github.com/$FULL.git"
  git push -u origin main
else
  echo "→ Creo $FULL (privato) e faccio il push di main"
  gh repo create "$FULL" --private --source=. --remote=origin --push \
    --description "Scopa e Briscola con coach AI (il Maestro) — Android nativo, motore Kotlin Multiplatform"
fi

echo "→ Impostazioni del repo: solo squash merge, branch cancellati dopo il merge, niente wiki"
gh api -X PATCH "repos/$FULL" \
  -F allow_squash_merge=true -F allow_merge_commit=false -F allow_rebase_merge=false \
  -F delete_branch_on_merge=true -F has_wiki=false >/dev/null

echo "→ Etichette per i task"
gh label create "fase-0"     --repo "$FULL" --color 1D76DB --description "Motore headless" --force
gh label create "bloccato"   --repo "$FULL" --color B60205 --description "Serve una risposta di Michele" --force
gh label create "agente"     --repo "$FULL" --color 5319E7 --description "PR aperta da un agente AI" --force

echo "→ Protezione di main: PR obbligatoria, CI verde, niente force-push"
if gh api -X PUT "repos/$FULL/branches/main/protection" --input - >/dev/null 2>&1 <<'JSON'
{
  "required_status_checks": { "strict": true, "contexts": ["check"] },
  "enforce_admins": false,
  "required_pull_request_reviews": { "required_approving_review_count": 0 },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON
then
  echo "  protezione attiva"
else
  echo "  ATTENZIONE: protezione non applicata. Sui repo privati serve GitHub Pro (o un'organizzazione Team)."
  echo "  Senza protezione, il controllo resta affidato alla revisione delle PR."
fi

echo "Fatto: https://github.com/$FULL"
