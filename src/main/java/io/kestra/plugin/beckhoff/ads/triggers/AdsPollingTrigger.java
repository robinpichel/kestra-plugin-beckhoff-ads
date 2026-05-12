package io.kestra.plugin.beckhoff.ads.triggers;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.conditions.ConditionContext;
import io.kestra.core.models.executions.Execution;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.triggers.AbstractTrigger;
import io.kestra.core.models.triggers.PollingTriggerInterface;
import io.kestra.core.models.triggers.TriggerContext;
import io.kestra.core.models.triggers.TriggerOutput;
import io.kestra.core.models.triggers.TriggerService;
import io.kestra.plugin.beckhoff.ads.client.AdsClient;
import io.kestra.plugin.beckhoff.ads.client.AdsClientFactory;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import io.kestra.plugin.beckhoff.ads.model.AdsException;
import io.kestra.plugin.beckhoff.ads.util.AdsTriggerEvaluator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(title = "Poll Beckhoff ADS variable and trigger on change or threshold")
@Plugin
public class AdsPollingTrigger extends AbstractTrigger implements PollingTriggerInterface, TriggerOutput<AdsPollingTrigger.Output> {
    private static final Map<String, Object> LAST_VALUES = new ConcurrentHashMap<>();

    @Builder.Default
    private final Duration interval = Duration.ofSeconds(30);

    @Schema(title = "ADS connection settings")
    private AdsConnection connection;

    @Schema(title = "PLC variable name", example = "GVL.Counter")
    private Property<String> variable;

    @Builder.Default
    @Schema(title = "ADS data type", example = "DINT")
    private Property<String> dataType = Property.ofValue("STRING");

    @Builder.Default
    @Schema(title = "Trigger mode", description = "ON_CHANGE, GT, LT, or EQ")
    private Property<String> mode = Property.ofValue("ON_CHANGE");

    @Schema(title = "Threshold value used by GT, LT, and EQ")
    private Property<String> threshold;

    @Override
    public Optional<Execution> evaluate(ConditionContext conditionContext, TriggerContext context) throws IllegalVariableEvaluationException {
        String variableName = conditionContext.getRunContext().render(variable).as(String.class).orElseThrow();
        String renderedType = conditionContext.getRunContext().render(dataType).as(String.class).orElse("STRING");
        String renderedMode = conditionContext.getRunContext().render(mode).as(String.class).orElse("ON_CHANGE").toUpperCase();
        String renderedThreshold = threshold == null ? null : conditionContext.getRunContext().render(threshold).as(String.class).orElse(null);
        AdsDataType resolvedType = AdsDataType.from(renderedType);

        Object currentValue;
        try {
            AdsClient client = AdsClientFactory.getClient();
            currentValue = client.read(connection, variableName, resolvedType);
        } catch (Exception e) {
            throw new AdsException("Failed to evaluate ADS trigger", e);
        }

        String stateKey = this.getId() + ":" + variableName;
        Object previousValue = LAST_VALUES.get(stateKey);
        LAST_VALUES.put(stateKey, currentValue);

        if (!AdsTriggerEvaluator.shouldTrigger(renderedMode, previousValue, currentValue, renderedThreshold)) {
            return Optional.empty();
        }

        Execution execution = TriggerService.generateExecution(
            this,
            conditionContext,
            context,
            Output.builder()
                .variable(variableName)
                .mode(renderedMode)
                .threshold(renderedThreshold)
                .previousValue(previousValue)
                .currentValue(currentValue)
                .build()
        );

        return Optional.of(execution);
    }
    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        private final String variable;
        private final String mode;
        private final String threshold;
        private final Object previousValue;
        private final Object currentValue;
    }
}
