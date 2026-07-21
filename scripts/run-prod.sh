#!/usr/bin/env bash
# Build and start the HRMS app in the PROD profile from the packaged JAR.
set -euo pipefail
cd "$(dirname "$0")/.."

# Load .env.prod if present (real environment variables take precedence).
if [[ -f .env.prod ]]; then
  set -a; source .env.prod; set +a
fi

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

# Fail fast if required secrets are missing.
: "${DB_URL:?DB_URL is required in prod}"
: "${DB_USERNAME:?DB_USERNAME is required in prod}"
: "${DB_PASSWORD:?DB_PASSWORD is required in prod}"

echo ">> Building production JAR..."
./gradlew clean bootJar -q

JAR="$(ls build/libs/hrms-*-SNAPSHOT.jar 2>/dev/null | grep -v plain | head -1)"
JAR="${JAR:-$(ls build/libs/hrms-*.jar | grep -v plain | head -1)}"

echo ">> Starting HRMS [profile=$SPRING_PROFILES_ACTIVE] on port ${SERVER_PORT:-8080} from $JAR"
exec java ${JAVA_OPTS:-} -jar "$JAR"
