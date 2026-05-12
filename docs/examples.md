# Beckhoff ADS Plugin Examples

This page contains runnable flow snippets for each plugin function.

## Shared Connection Fields

Most tasks and triggers use the same `connection` object:

```yaml
connection:
  targetIp: "192.168.0.20" # optional, mainly used for route setup
  targetAmsNetId: "5.32.176.1.1.1"
  targetAmsPort: 851
  localAmsNetId: "192.168.0.10.1.1" # optional
  autoAddRoute: false
  timeout: PT5S
  stringReadLength: 256
```

## 1) Read Task

Reads one ADS variable and exposes the typed value in task outputs.

```yaml
id: ads-read-example
namespace: io.kestra.plugin.beckhoff.ads

tasks:
  - id: read-counter
    type: io.kestra.plugin.beckhoff.ads.tasks.Read
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "MAIN.nAdsKestraTest"
    dataType: "DINT"

  - id: log-read
    type: io.kestra.plugin.core.log.Log
    message: "Read value: {{ outputs['read-counter'].value }}"
```

## 2) Write Task

Writes a value to one ADS variable.

```yaml
id: ads-write-example
namespace: io.kestra.plugin.beckhoff.ads

tasks:
  - id: write-counter
    type: io.kestra.plugin.beckhoff.ads.tasks.Write
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "MAIN.nAdsKestraTest"
    dataType: "DINT"
    value: 42
```

Write using output from another task:

```yaml
id: ads-read-then-write
namespace: io.kestra.plugin.beckhoff.ads

tasks:
  - id: read-source
    type: io.kestra.plugin.beckhoff.ads.tasks.Read
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "MAIN.SourceValue"
    dataType: "DINT"

  - id: write-target
    type: io.kestra.plugin.beckhoff.ads.tasks.Write
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "MAIN.TargetValue"
    dataType: "DINT"
    value: "{{ outputs['read-source'].value }}"
```

## 3) DiscoverSymbols Task

Discovers available ADS symbols from the target runtime.

```yaml
id: ads-discover-symbols
namespace: io.kestra.plugin.beckhoff.ads

tasks:
  - id: discover
    type: io.kestra.plugin.beckhoff.ads.tasks.DiscoverSymbols
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851

  - id: log-count
    type: io.kestra.plugin.core.log.Log
    message: "Discovered symbols: {{ outputs['discover'].count }}"
```

## 4) PollingTrigger

### ON_CHANGE

Starts a new execution when the value changes.

```yaml
id: ads-trigger-on-change
namespace: io.kestra.plugin.beckhoff.ads

triggers:
  - id: plc-changed
    type: io.kestra.plugin.beckhoff.ads.triggers.PollingTrigger
    interval: PT10S
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "MAIN.nAdsKestraTest"
    dataType: "DINT"
    mode: "ON_CHANGE"

tasks:
  - id: log
    type: io.kestra.plugin.core.log.Log
    message: "Value changed to {{ trigger.outputs.currentValue }}"
```

### GT (greater than)

Starts when current value is greater than threshold.

```yaml
id: ads-trigger-gt
namespace: io.kestra.plugin.beckhoff.ads

triggers:
  - id: plc-gt
    type: io.kestra.plugin.beckhoff.ads.triggers.PollingTrigger
    interval: PT10S
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "MAIN.Temperature"
    dataType: "REAL"
    mode: "GT"
    threshold: "70.5"
```

### LT (lower than)

Starts when current value is lower than threshold.

```yaml
id: ads-trigger-lt
namespace: io.kestra.plugin.beckhoff.ads

triggers:
  - id: plc-lt
    type: io.kestra.plugin.beckhoff.ads.triggers.PollingTrigger
    interval: PT10S
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "MAIN.Pressure"
    dataType: "REAL"
    mode: "LT"
    threshold: "1.2"
```

### EQ (equals)

Starts when current value equals threshold.

```yaml
id: ads-trigger-eq
namespace: io.kestra.plugin.beckhoff.ads

triggers:
  - id: plc-eq
    type: io.kestra.plugin.beckhoff.ads.triggers.PollingTrigger
    interval: PT10S
    connection:
      targetAmsNetId: "5.32.176.1.1.1"
      targetAmsPort: 851
    variable: "MAIN.StateCode"
    dataType: "INT"
    mode: "EQ"
    threshold: "5"
```
