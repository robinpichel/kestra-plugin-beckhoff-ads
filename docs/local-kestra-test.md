# Local Kestra Test Guide

Use this guide to run the plugin in a local Kestra instance and execute one ADS smoke flow.

## Prerequisites

- Docker Desktop (or Docker Engine with Compose)
- Reachable ADS target (for example local TwinCAT runtime)
- On Windows for host-side ADS tests: `AdsToJava-3.dll` available (the quick test script can download it automatically)

## 1) Build plugin JAR

From repository root:

```powershell
./gradlew.bat clean shadowJar --no-daemon -x test -x jacocoTestReport
```

Expected output artifact:

- `build/libs/plugin-beckhoff-ads-1.0.0-SNAPSHOT.jar`

## 2) Start Kestra with the local plugin

The compose setup mounts `build/libs` into `/app/plugins` inside Kestra.

```powershell
docker compose up -d --build
```

Open Kestra UI:

- http://localhost:8080

## 3) Create and run the smoke flow

Use flow file:

- `examples/flows/ads-local-smoke.yaml`

In Kestra UI:

1. Go to Flows.
2. Create flow.
3. Paste content from `examples/flows/ads-local-smoke.yaml`.
4. Save.
5. Run once.

Expected result:

- `read-before`, `write-value`, and `read-after` complete successfully.
- `log-result` prints values showing the write path worked.

## 4) Optional: quick host ADS validation

If you want to validate ADS connectivity outside Kestra first:

```powershell
./scripts/run-real-ads-quicktest.ps1
```

## Troubleshooting

- Plugin task type not found:
  - Rebuild JAR and restart compose.
  - Confirm JAR exists in `build/libs`.
- ADS read/write fails:
  - Verify AMS Net ID, ADS port, and PLC variable name.
  - Ensure route/network permissions allow ADS communication.
- Native library issues:
  - Ensure ADS native library is available in runtime environment.

## Stop environment

```powershell
docker compose down
```
