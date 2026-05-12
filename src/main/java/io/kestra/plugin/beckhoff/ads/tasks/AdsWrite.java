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
import io.kestra.plugin.beckhoff.ads.util.AdsValueConverter;
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
@Schema(title = "Write a value to a Beckhoff ADS variable")
@Plugin
public class AdsWrite extends Task implements RunnableTask<AdsWrite.Output> {
    @Schema(title = "ADS connection settings")
    private AdsConnection connection;

    @Schema(title = "PLC variable name", example = "GVL.Counter")
    private Property<String> variable;

    @Schema(title = "Value to write")
    private Property<Object> value;

    @Builder.Default
    @Schema(title = "ADS data type", example = "DINT")
    private Property<String> dataType = Property.ofValue("STRING");

    @Override
    public Output run(RunContext runContext) throws Exception {
        String variableName = runContext.render(variable).as(String.class).orElseThrow();
        String renderedType = runContext.render(dataType).as(String.class).orElse("STRING");
        AdsDataType resolvedType = AdsDataType.from(renderedType);
        Object renderedValue = runContext.render(value).as(Object.class).orElse(null);

        try {
            AdsClient client = AdsClientFactory.getClient();
            Object converted = AdsValueConverter.convertInput(renderedValue, resolvedType);
            client.write(connection, variableName, resolvedType, converted);
            return Output.builder()
                .variable(variableName)
                .dataType(resolvedType.name())
                .value(converted)
                .written(true)
                .build();
        } catch (Exception e) {
            throw new AdsException("Failed to write ADS variable '" + variableName + "'", e);
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        private final String variable;
        private final String dataType;
        private final Object value;
        private final boolean written;
    }
}
