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

## Notes on ADS library integration

The current implementation uses an ADS client provider abstraction. Plug in your preferred Beckhoff ADS Java library through AdsClientFactory.setProvider so tasks and triggers can execute against your runtime adapter.

## License

Apache 2.0
