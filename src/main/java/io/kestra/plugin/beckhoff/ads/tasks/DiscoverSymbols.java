package io.kestra.plugin.beckhoff.ads.tasks;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.beckhoff.ads.client.AdsClient;
import io.kestra.plugin.beckhoff.ads.client.AdsClientFactory;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsException;
import io.kestra.plugin.beckhoff.ads.model.AdsSymbol;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Discover symbols on Beckhoff ADS",
    description = "Lists symbols exposed by the ADS target."
)
@Plugin
public class DiscoverSymbols extends Task implements RunnableTask<DiscoverSymbols.Output> {
    @Schema(title = "ADS connection settings")
    private AdsConnection connection;

    @Override
    public Output run(RunContext runContext) throws Exception {
        try {
            AdsClient client = AdsClientFactory.getClient();
            List<AdsSymbol> symbols = client.discoverSymbols(connection);
            return Output.builder()
                .symbols(symbols)
                .count(symbols.size())
                .build();
        } catch (Exception e) {
            throw new AdsException("Failed to discover ADS symbols", e);
        }
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        private final List<AdsSymbol> symbols;
        private final Integer count;
    }
}
