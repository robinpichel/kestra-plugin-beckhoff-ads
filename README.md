# Kestra Beckhoff ADS Plugin

Community plugin for Beckhoff ADS communication in Kestra.

Important: this repository is an unofficial community project and is not affiliated with or endorsed by Beckhoff.

## Scope

- Run ADS reads from flows.
- Run ADS writes from flows.
- Discover ADS symbols from a target.
- Trigger flows using polling with modes: ON_CHANGE, GT, LT, EQ.

## Package layout

- io.kestra.plugin.beckhoff.ads.tasks
- io.kestra.plugin.beckhoff.ads.triggers
- io.kestra.plugin.beckhoff.ads.client
- io.kestra.plugin.beckhoff.ads.config
- io.kestra.plugin.beckhoff.ads.model
- io.kestra.plugin.beckhoff.ads.util

## Example

```yaml
id: ads-read-write
namespace: io.kestra.plugin.beckhoff.ads

tasks:
  - id: read-counter
    type: io.kestra.plugin.beckhoff.ads.tasks.AdsRead
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "GVL.Counter"
    dataType: "DINT"

  - id: write-counter
    type: io.kestra.plugin.beckhoff.ads.tasks.AdsWrite
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "GVL.Counter"
    value: "{{ outputs['read-counter'].value }}"
    dataType: "DINT"
```

## Development

- Build and test with ./gradlew clean test.
- Test flows are in src/test/resources/flows.

### Real ADS quick test (Windows host)

Use this when your PLC is reachable from the same Windows host where you run the build.

Default live target settings:

- ADS_TARGET_IP=localhost
- ADS_TARGET_AMS_NET_ID=199.4.42.250.1.1
- ADS_TARGET_AMS_PORT=851
- ADS_REAL_VARIABLE=MAIN.nAdsKestraTest

Recommended one-command run:

```powershell
./scripts/run-real-ads-quicktest.ps1
```

Custom run example:

```powershell
./scripts/run-real-ads-quicktest.ps1 `
  -TargetIp localhost `
  -TargetAmsNetId 199.4.42.250.1.1 `
  -TargetAmsPort 851 `
  -Variable MAIN.nAdsKestraTest `
  -DataType DINT
```

Manual fast run (if you need full control):

```bash
ADS_REAL_SMOKE_TEST=true \
ADS_TARGET_IP=localhost \
ADS_TARGET_AMS_NET_ID=199.4.42.250.1.1 \
ADS_TARGET_AMS_PORT=851 \
ADS_REAL_VARIABLE=MAIN.nAdsKestraTest \
./gradlew realAdsQuickTest
```

If the variable does not change, check the test output for "ADS error code". When handle resolution fails first, the write operation is never executed.

## Notes on ADS library integration

The plugin now uses Beckhoff's official AdsToJava library:

- Jar: libs/TcJavaToAds-3.1.0.jar (from official Beckhoff release 3.1.0-32)
- JNI wrapper class: de.beckhoff.jni.tcads.AdsCallDllFunction

Runtime requirement:

- The native library AdsToJava-3.dll (Windows) or libAdsToJava-3.so (Linux/TcBSD) must be available in java.library.path.

Advanced usage:

- You can still override the client provider via AdsClientFactory.setProvider for custom adapters or tests.

## License

Apache 2.0
