# Datagen → Kafka → MongoDB pipeline

A pure **Kafka Connect** pipeline (no Java code): the Confluent **Datagen source
connector** synthesises IoT sensor readings from a crafted Avro schema and writes
them to a Kafka topic; the **MongoDB sink connector** upserts them into MongoDB.

```
┌──────────────────────┐   Avro value   ┌─────────────────────────┐   upsert by    ┌───────────────┐
│ Datagen Source        │  ───────────▶  │ topic:                  │   deviceId     │ MongoDB       │
│ Connector             │  String key    │ de.bluesharp.model.     │  ────────────▶ │ sensors       │
│ schema=SensorReading  │  (deviceId)    │ SensorReading           │  (Mongo Sink   │ .readings     │
│ keyfield=deviceId     │                │ (Schema Registry :8085) │   Connector)   │               │
└──────────────────────┘                └─────────────────────────┘                └───────────────┘
       deviceId is drawn from an 8-value pool → keys repeat → the upsert collapses the
       stream to ~8 documents, one per device, each overwritten as newer readings arrive.
```

## Why Kafka Connect?

The Confluent Datagen generator ships as a **Kafka Connect source connector**
(`kafka-connect-datagen`) — it runs inside a Connect worker, not as a standalone app.
So this feature adds a `kafka-connect` worker to `docker-compose.yml`; the worker
installs both plugins (datagen + MongoDB) from Confluent Hub on start-up.

## The model — `schemas/sensor-reading.avsc`

`SensorReading` deliberately exercises **every Avro type plus nesting**, so the
document that lands in MongoDB is rich and varied:

| Field          | Avro type                              | Notes |
|----------------|----------------------------------------|-------|
| `deviceId`     | string (from an 8-value pool)          | business key; repeats drive the upsert |
| `version`      | long                                   | revision counter stored on the document |
| `sensorType`   | enum                                   | `TEMPERATURE \| HUMIDITY \| PRESSURE \| MOTION` |
| `temperature`  | double                                 | ranged |
| `humidity`     | double                                 | ranged |
| `battery`      | int                                    | 0–100 |
| `online`       | boolean                                | |
| `readAt`       | long, `timestamp-millis` logical type  | stored as a BSON **Date** |
| `signalSample` | bytes                                  | stored as BSON binary |
| `tags`         | array&lt;string&gt;                    | |
| `location`     | nested record                          | latitude/longitude/building/floor |
| `metrics`      | array of nested record `Metric`        | deep nesting (name/value/unit-enum) |
| `attributes`   | map&lt;string,string&gt;               | |

## What "upsert" and "version" mean here

You asked whether Kafka Connect / MongoDB offer a **native version-gated** upsert
("apply only if the incoming `version` ≥ the stored one"). They don't:

- **MongoDB** has no built-in document versioning / optimistic concurrency — that's
  a design pattern, not a server feature.
- The **MongoDB sink connector's** shipped `writemodel.strategy` implementations all
  do plain / business-key upserts (**last-write-wins**); none is version-gated.
  True gating would require a **custom `WriteModelStrategy` Java class** on the
  plugin path — which defeats the zero-Java approach.

So this pipeline uses the connector's **native business-key upsert**:

```
document.id.strategy      = PartialValueStrategy over [deviceId]  →  _id = { deviceId }
writemodel.strategy       = ReplaceOneDefaultStrategy             →  replaceOne(_id, doc, upsert=true)
```

The `version` field is stored on the document for visibility; on each new reading for
a device the whole document (including `version` and `readAt`) is replaced by the
latest message. If you later want "higher version wins", the isolated add-on is a
custom `WriteModelStrategy` — everything else stays the same.

## Run it

```bash
# from the project root (spring-kafka-simple-json/)
docker compose up -d                    # brings up kafka, schema-registry, connect, mongodb, mongo-express, akhq
./connect/register-connectors.sh        # waits for Connect + plugins, then registers both connectors
```

Then watch it flow:

- **Kafka topic** — AKHQ at <http://localhost:8080> → topic `de.bluesharp.model.SensorReading`
- **MongoDB** — mongo-express at <http://localhost:8081> → database `sensors`, collection `readings`
  (expect ~8 documents, one per `deviceId`, with `version` / `readAt` advancing as upserts land)

Or from the shell:

```bash
docker compose exec mongodb mongosh sensors --quiet \
  --eval 'db.readings.countDocuments()' \
  --eval 'db.readings.findOne()'
```

## Files

| File | Purpose |
|------|---------|
| `schemas/sensor-reading.avsc` | the crafted `SensorReading` model (source of truth) |
| `datagen-sensor-source.json`  | Datagen source connector config |
| `mongo-sensor-sink.json`      | MongoDB sink connector config (upsert by `deviceId`) |
| `register-connectors.sh`      | idempotent registration against the Connect REST API |
