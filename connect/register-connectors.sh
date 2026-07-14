#!/usr/bin/env bash
#
# Registers (or updates) the datagen source and MongoDB sink connectors on the
# local Kafka Connect worker. Idempotent: safe to re-run — it PUTs each config,
# so an existing connector is updated in place instead of erroring.
#
# Requires: curl, jq. Usage: ./register-connectors.sh
#
set -euo pipefail

CONNECT_URL="${CONNECT_URL:-http://localhost:8083}"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIGS=("$HERE/datagen-sensor-source.json" "$HERE/mongo-sensor-sink.json")

echo "Waiting for Kafka Connect at $CONNECT_URL ..."
until curl -sf "$CONNECT_URL/connectors" >/dev/null 2>&1; do
  printf '.'
  sleep 3
done
echo " up."

# Plugins are installed from Confluent Hub on worker start-up, which can lag the
# REST endpoint coming up. Wait until both plugin classes are actually loaded.
echo "Waiting for connector plugins to load ..."
until curl -sf "$CONNECT_URL/connector-plugins" | jq -e \
      'map(.class) | index("io.confluent.kafka.connect.datagen.DatagenConnector")
        and index("com.mongodb.kafka.connect.MongoSinkConnector")' >/dev/null 2>&1; do
  printf '.'
  sleep 3
done
echo " loaded."

for cfg in "${CONFIGS[@]}"; do
  name="$(jq -r '.name' "$cfg")"
  echo "Registering connector: $name"
  jq '.config' "$cfg" | curl -sf -X PUT \
    -H 'Content-Type: application/json' \
    --data @- \
    "$CONNECT_URL/connectors/$name/config" >/dev/null
  echo "  -> submitted"
done

echo
echo "Connector status:"
for cfg in "${CONFIGS[@]}"; do
  name="$(jq -r '.name' "$cfg")"
  # Give the tasks a moment to transition out of UNASSIGNED before reporting.
  sleep 3
  # On a cold worker a task can momentarily FAIL if the connector is created
  # before the plugin finished initialising; restart any failed task once.
  if curl -sf "$CONNECT_URL/connectors/$name/status" \
       | jq -e '[.tasks[].state] | index("FAILED")' >/dev/null 2>&1; then
    echo "  ($name had a failed task on start-up; restarting it)"
    curl -sf -X POST "$CONNECT_URL/connectors/$name/restart?includeTasks=true&onlyFailed=true" >/dev/null || true
    sleep 3
  fi
  curl -sf "$CONNECT_URL/connectors/$name/status" \
    | jq -r '"  \(.name): connector=\(.connector.state) task=\([.tasks[].state] | join(","))"'
done

echo
echo "Done. Inspect the pipeline:"
echo "  Kafka topic (AKHQ):   http://localhost:8080"
echo "  MongoDB (mongo-express): http://localhost:8081  (db 'sensors', collection 'readings')"
