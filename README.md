# Kestra Beckhoff ADS Plugin

Important: this repository is an unofficial community project and is not affiliated with or endorsed by Beckhoff.

## Why

- What user problem does this solve? Teams need to read, write, and poll PLC values over Beckhoff ADS from orchestrated workflows instead of maintaining ad hoc scripts and disconnected integrations.
- Why would a team adopt this plugin in a workflow? It keeps ADS operations in the same Kestra flow as retries, conditions, notifications, and downstream automation.
- What operational/business outcome does it enable? It improves traceability and reliability for PLC-driven processes while reducing manual steps.

## What

- Provides plugin components under `io.kestra.plugin.beckhoff.ads`.
- Includes classes such as `Read`, `Write`, `DiscoverSymbols`, and `PollingTrigger`.

### Feature summary

- `Read`: reads a typed ADS variable and returns `variable`, `dataType`, and `value`.
- `Write`: writes a typed ADS value and returns `variable`, `dataType`, `value`, and `written`.
- `DiscoverSymbols`: lists symbols from the target and returns `symbols` plus `count`.
- `PollingTrigger`: polls a variable and starts executions for `ON_CHANGE`, `GT`, `LT`, or `EQ` conditions.

## Example

```yaml
id: ads-read-write
namespace: io.kestra.plugin.beckhoff.ads

tasks:
  - id: read-counter
    type: io.kestra.plugin.beckhoff.ads.tasks.Read
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "GVL.Counter"
    dataType: "DINT"

  - id: write-counter
    type: io.kestra.plugin.beckhoff.ads.tasks.Write
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "GVL.Counter"
    value: "{{ outputs['read-counter'].value }}"
    dataType: "DINT"
```

## Documentation

* Full documentation can be found under [kestra.io/docs](https://kestra.io/docs)
* Documentation for developing a plugin is included in the [Plugin Developer Guide](https://kestra.io/docs/plugin-developer-guide/)
* End-to-end examples for every function are available in [docs/examples.md](docs/examples.md)

## Local development

- Run the standard test suite: `./gradlew clean test`
- Run the fast real ADS host test: `./gradlew realAdsQuickTest`

### Real ADS quick test (Windows host)

Use this mode when your PLC is reachable from the same Windows host where the tests run.

Default live target settings:

- `ADS_TARGET_IP=localhost`
- `ADS_TARGET_AMS_NET_ID=199.4.42.250.1.1`
- `ADS_TARGET_AMS_PORT=851`
- `ADS_REAL_VARIABLE=MAIN.nAdsKestraTest`

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

If the PLC variable does not change, check the output for `ADS error code`. If handle resolution fails first, the write operation is never executed.

## AdsToJava runtime requirements

- Java artifact: `libs/TcJavaToAds-3.1.0.jar`
- Native library required in `java.library.path`:
  - Windows: `AdsToJava-3.dll`
  - Linux/TcBSD: `libAdsToJava-3.so`

## License
Apache 2.0
