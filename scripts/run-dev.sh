#!/usr/bin/env bash
# =============================================================================
# HRMS — official local development entry point.
# Loads env, verifies prerequisites, then starts the app (dev profile, devtools).
#
#   cp .env.example .env   # then edit DB_* and SUPER_ADMIN_*
#   ./scripts/run-dev.sh
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")/.."

# --- colours (fall back to plain if not a TTY) -------------------------------
if [[ -t 1 ]]; then B="\033[1m"; G="\033[32m"; Y="\033[33m"; R="\033[31m"; C="\033[36m"; N="\033[0m"; else B=""; G=""; Y=""; R=""; C=""; N=""; fi
info() { echo -e "${C}>>${N} $*"; }
ok()   { echo -e "${G}[OK]${N} $*"; }
warn() { echo -e "${Y}[!]${N} $*"; }
fail() { echo -e "${R}[X] $*${N}" >&2; }

echo -e "${B}=== HRMS local dev startup ===${N}"

# --- 1. Load environment (.env first, then .env.dev overrides) ---------------
loaded=""
for f in .env .env.dev; do
  if [[ -f "$f" ]]; then
    # Load without aborting on a single malformed line (e.g. an unquoted value
    # containing spaces); warn instead so startup is resilient.
    set +e; set -a; source "$f"; rc=$?; set +a; set -e
    [[ $rc -ne 0 ]] && warn "$f loaded with warnings — quote any values containing spaces (FOO=\"a b\")."
    loaded="${loaded:+$loaded, }$f"
  fi
done
if [[ -n "$loaded" ]]; then
  ok "Loaded environment from: $loaded"
else
  warn "No .env or .env.dev found — using built-in defaults. Run: cp .env.example .env"
fi

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
export DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/hrms}"

# --- 2. Verify required variables --------------------------------------------
missing=0
require() { # $1 = var name
  if [[ -z "${!1:-}" ]]; then fail "Required variable $1 is not set"; missing=1; else ok "$1 is set"; fi
}
require DB_URL
require DB_USERNAME
[[ -z "${DB_PASSWORD:-}" ]] && warn "DB_PASSWORD is empty (ok only if your DB uses trust/peer auth)"

if [[ -z "${SUPER_ADMIN_EMAIL:-}" || -z "${SUPER_ADMIN_PASSWORD:-}" ]]; then
  warn "SUPER_ADMIN_EMAIL/SUPER_ADMIN_PASSWORD not both set — no Super Admin will be bootstrapped."
  warn "You will not be able to log in until an admin user exists (see .env.example)."
else
  ok "Super Admin bootstrap configured for: ${SUPER_ADMIN_EMAIL}"
fi

if [[ "$missing" -ne 0 ]]; then
  fail "Missing required configuration. Edit your .env (copy from .env.example) and retry."
  exit 1
fi

# --- 3. Verify PostgreSQL is reachable ---------------------------------------
# Parse host:port/db from a jdbc:postgresql://host:port/db URL.
db_hostport="${DB_URL#jdbc:postgresql://}"
db_host="${db_hostport%%:*}"
db_rest="${db_hostport#*:}"
db_port="${db_rest%%/*}"
db_name="${db_rest#*/}"; db_name="${db_name%%\?*}"
db_host="${db_host:-localhost}"; db_port="${db_port:-5432}"

info "Checking PostgreSQL at ${db_host}:${db_port} (database '${db_name}')..."
pg_up=0
if command -v pg_isready >/dev/null 2>&1; then
  pg_isready -h "$db_host" -p "$db_port" >/dev/null 2>&1 && pg_up=1
else
  # No pg_isready — fall back to a raw TCP connection attempt.
  (exec 3<>"/dev/tcp/${db_host}/${db_port}") >/dev/null 2>&1 && pg_up=1 && exec 3>&- || true
fi
if [[ "$pg_up" -eq 1 ]]; then
  ok "PostgreSQL is accepting connections"
else
  fail "PostgreSQL is NOT reachable at ${db_host}:${db_port}."
  echo   "  Start it, e.g.:"
  echo   "    docker run --name hrms-postgres -e POSTGRES_DB=${db_name} -e POSTGRES_USER=${DB_USERNAME} \\"
  echo   "      -e POSTGRES_PASSWORD=<pw> -p ${db_port}:5432 -d postgres:17"
  echo   "  and create the database if needed:  createdb -h ${db_host} -p ${db_port} ${db_name}"
  exit 1
fi

# --- 4. Start the application ------------------------------------------------
echo -e "${B}=== Starting HRMS [profile=${SPRING_PROFILES_ACTIVE}] on port ${SERVER_PORT:-8080} ===${N}"
info "Swagger UI : http://localhost:${SERVER_PORT:-8080}/swagger-ui.html"
info "Health     : http://localhost:${SERVER_PORT:-8080}/actuator/health"
info "Verify     : ./scripts/verify-dev.sh   (in another terminal, once startup completes)"
exec ./gradlew bootRun
