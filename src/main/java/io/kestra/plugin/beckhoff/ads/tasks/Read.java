package io.kestra.plugin.beckhoff.ads.tasks;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.beckhoff.ads.client.AdsClient;
import io.kestra.plugin.beckhoff.ads.client.AdsClientFactory;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import io.kestra.plugin.beckhoff.ads.model.AdsException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Read a variable from Beckhoff ADS",
    description = "Reads a variable from an ADS target and returns the typed value."
)
@Plugin(examples = {
    @io.kestra.core.models.annotations.Example(
        title = "Read a PLC variable",
        code = {
            "connection:",
            "  targetAmsNetId: \"5.32.176.1.1.1\"",
            "  targetAmsPort: 851",
            "variable: \"GVL.Counter\"",
            "dataType: \"DINT\""
        }
    )
})
public class Read extends Task implements RunnableTask<Read.Output> {
    @Schema(title = "ADS connection settings")
    private AdsConnection connection;

    @Schema(title = "PLC variable name", example = "GVL.Counter")
    private Property<String> variable;

    @Builder.Default
    @Schema(title = "ADS data type", example = "DINT")
    private Property<String> dataType = Property.ofValue("STRING");

    @Override
    public Output run(RunContext runContext) throws Exception {
        String variableName = runContext.render(variable).as(String.class).orElseThrow();
        String renderedType = runContext.render(dataType).as(String.class).orElse("STRING");
        AdsDataType resolvedType = AdsDataType.from(renderedType);

        try {
            AdsClient client = AdsClientFactory.getClient();
            Object value = client.read(connection, variableName, resolvedType);
            return Output.builder()
                .variable(variableName)
                .dataType(resolvedType.name())
                .value(value)
                .build();
        } catch (Exception e) {
            throw new AdsException("Failed to read ADS variable '" + variableName + "'", e);
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        private final String variable;
        private final String dataType;
        private final Object value;
    }
}
