#!/usr/bin/env bash
# =============================================================================
# HRMS — local development verification.
# Checks the running dev environment end-to-end and reports PASS/FAIL per item.
# Run AFTER ./scripts/run-dev.sh has fully started the app.
#
#   ./scripts/verify-dev.sh
#
# Exit code 0 = all checks passed; non-zero = at least one FAIL.
# =============================================================================
set -uo pipefail
cd "$(dirname "$0")/.."

if [[ -t 1 ]]; then B="\033[1m"; G="\033[32m"; R="\033[31m"; Y="\033[33m"; N="\033[0m"; else B=""; G=""; R=""; Y=""; N=""; fi

PASS=0; FAIL=0
pass() { echo -e "  ${G}PASS${N} $*"; PASS=$((PASS+1)); }
crit() { echo -e "  ${R}FAIL${N} $*"; FAIL=$((FAIL+1)); }
skip() { echo -e "  ${Y}WARN${N} $*"; }

echo -e "${B}=== HRMS dev environment verification ===${N}"

# --- Load env (same order as run-dev.sh) -------------------------------------
for f in .env .env.dev; do [[ -f "$f" ]] && { set -a; source "$f"; set +a; }; done
PORT="${SERVER_PORT:-8080}"
BASE="${HRMS_BASE_URL:-http://localhost:${PORT}}"
DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/hrms}"

# Parse host/port/db from the JDBC URL.
hp="${DB_URL#jdbc:postgresql://}"; db_host="${hp%%:*}"; rest="${hp#*:}"
db_port="${rest%%/*}"; db_name="${rest#*/}"; db_name="${db_name%%\?*}"
db_host="${db_host:-localhost}"; db_port="${db_port:-5432}"

psql_q() { # $1 = SQL -> prints single value
  PGPASSWORD="${DB_PASSWORD:-}" psql -h "$db_host" -p "$db_port" -U "${DB_USERNAME:-postgres}" \
    -d "$db_name" -tAc "$1" 2>/dev/null
}

echo -e "\n${B}[1] Environment variables${N}"
if [[ -n "${DB_URL:-}" && -n "${DB_USERNAME:-}" ]]; then pass "DB_URL and DB_USERNAME loaded"; else crit "DB_URL/DB_USERNAME not loaded"; fi
if [[ -n "${SUPER_ADMIN_EMAIL:-}" && -n "${SUPER_ADMIN_PASSWORD:-}" ]]; then pass "SUPER_ADMIN_* loaded (${SUPER_ADMIN_EMAIL})"; else skip "SUPER_ADMIN_* not both set — login check will be skipped"; fi

echo -e "\n${B}[2] Database connectivity${N}"
if command -v psql >/dev/null 2>&1; then
  if [[ "$(psql_q 'SELECT 1;')" == "1" ]]; then pass "Connected to ${db_host}:${db_port}/${db_name}"; else crit "Cannot connect to ${db_host}:${db_port}/${db_name}"; fi
else
  skip "psql not installed — skipping direct DB checks (health check still validates DB)"
fi

echo -e "\n${B}[3] Flyway migrations${N}"
if command -v psql >/dev/null 2>&1; then
  applied="$(psql_q "SELECT count(*) FROM flyway_schema_history WHERE success = true;")"
  failedm="$(psql_q "SELECT count(*) FROM flyway_schema_history WHERE success = false;")"
  if [[ -n "$applied" && "$applied" -ge 1 ]]; then pass "$applied migration(s) applied successfully"; else crit "No successful Flyway migrations found (is the schema migrated?)"; fi
  if [[ -n "$failedm" && "$failedm" -ne 0 ]]; then crit "$failedm FAILED migration(s) in flyway_schema_history"; fi
else
  skip "psql not installed — cannot inspect flyway_schema_history"
fi

echo -e "\n${B}[4] Seeded roles & permissions${N}"
if command -v psql >/dev/null 2>&1; then
  roles="$(psql_q 'SELECT count(*) FROM roles;')"
  perms="$(psql_q 'SELECT count(*) FROM permissions;')"
  if [[ -n "$roles" && "$roles" -ge 9 ]]; then pass "$roles roles seeded"; else crit "Expected >= 9 roles, found '${roles:-none}'"; fi
  if [[ -n "$perms" && "$perms" -ge 1 ]]; then pass "$perms permissions seeded"; else crit "No permissions seeded"; fi
else
  skip "psql not installed — cannot verify role/permission seeding"
fi

echo -e "\n${B}[5] Super Admin bootstrap${N}"
if [[ -n "${SUPER_ADMIN_EMAIL:-}" ]] && command -v psql >/dev/null 2>&1; then
  cnt="$(psql_q "SELECT count(*) FROM users WHERE lower(email) = lower('${SUPER_ADMIN_EMAIL}');")"
  if [[ "$cnt" == "1" ]]; then pass "Super Admin user exists (${SUPER_ADMIN_EMAIL})"; else crit "Super Admin '${SUPER_ADMIN_EMAIL}' not found (found '${cnt:-0}')"; fi
else
  skip "Skipping DB check for Super Admin (needs SUPER_ADMIN_EMAIL + psql)"
fi

echo -e "\n${B}[6] Health endpoint${N}"
health="$(curl -fsS "${BASE}/actuator/health" 2>/dev/null || true)"
if echo "$health" | grep -q '"status":"UP"'; then pass "GET /actuator/health -> UP"; else crit "GET /actuator/health did not report UP (got: ${health:-<no response>})"; fi

echo -e "\n${B}[7] Login endpoint${N}"
token=""
if [[ -n "${SUPER_ADMIN_EMAIL:-}" && -n "${SUPER_ADMIN_PASSWORD:-}" ]]; then
  resp="$(curl -fsS -X POST "${BASE}/api/v1/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"${SUPER_ADMIN_EMAIL}\",\"password\":\"${SUPER_ADMIN_PASSWORD}\"}" 2>/dev/null || true)"
  token="$(printf '%s' "$resp" | grep -o '"accessToken":"[^"]*"' | head -1 | sed 's/.*:"//;s/"$//')"
  if [[ -n "$token" ]]; then pass "POST /api/v1/auth/login returned an access token"; else crit "Login failed for ${SUPER_ADMIN_EMAIL} (response: ${resp:-<none>})"; fi
else
  skip "SUPER_ADMIN_* not set — cannot exercise login"
fi

echo -e "\n${B}[8] Authenticated request (token works)${N}"
if [[ -n "$token" ]]; then
  code="$(curl -s -o /dev/null -w '%{http_code}' "${BASE}/api/v1/auth/me" -H "Authorization: Bearer ${token}")"
  if [[ "$code" == "200" ]]; then pass "GET /api/v1/auth/me with token -> 200"; else crit "GET /api/v1/auth/me -> ${code} (expected 200)"; fi
else
  skip "No token — skipping authenticated-request check"
fi

echo -e "\n${B}[9] OpenAPI / Swagger${N}"
api_code="$(curl -s -o /dev/null -w '%{http_code}' "${BASE}/v3/api-docs")"
if [[ "$api_code" == "200" ]]; then pass "GET /v3/api-docs -> 200"; else crit "GET /v3/api-docs -> ${api_code} (expected 200)"; fi
ui_code="$(curl -s -o /dev/null -w '%{http_code}' -L "${BASE}/swagger-ui/index.html")"
if [[ "$ui_code" == "200" ]]; then pass "Swagger UI reachable (/swagger-ui/index.html -> 200)"; else skip "Swagger UI returned ${ui_code} (api-docs is the primary check)"; fi

# --- Summary -----------------------------------------------------------------
echo -e "\n${B}=== Summary: ${G}${PASS} passed${N}${B}, ${R}${FAIL} failed${N}${B} ===${N}"
if [[ "$FAIL" -ne 0 ]]; then
  echo -e "${R}Verification FAILED.${N} If the app isn't started yet, run ./scripts/run-dev.sh first."
  exit 1
fi
echo -e "${G}All checks passed — the local dev environment is healthy.${N}"
