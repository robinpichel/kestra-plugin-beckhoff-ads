package io.kestra.plugin.beckhoff.ads.tasks;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.beckhoff.ads.TestAdsClient;
import io.kestra.plugin.beckhoff.ads.client.AdsClientFactory;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsSymbol;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@KestraTest
class DiscoverSymbolsTest {
    @Inject
    private RunContextFactory runContextFactory;

    private TestAdsClient testClient;

    @BeforeEach
    void setup() {
        testClient = new TestAdsClient();
        testClient.setSymbols(List.of(
            AdsSymbol.builder().name("GVL.Counter").type("DINT").size(4).build(),
            AdsSymbol.builder().name("GVL.Enabled").type("BOOL").size(1).build()
        ));
        AdsClientFactory.setProvider(() -> testClient);
    }

    @AfterEach
    void cleanup() {
        AdsClientFactory.resetProvider();
    }

    @Test
    void run() throws Exception {
        RunContext runContext = runContextFactory.of(Map.of());

        DiscoverSymbols task = DiscoverSymbols.builder()
            .connection(AdsConnection.builder().targetAmsNetId("5.32.176.1.1.1").build())
            .build();

        DiscoverSymbols.Output output = task.run(runContext);

        assertThat(output.getCount(), is(2));
        assertThat(output.getSymbols().get(0).getName(), is("GVL.Counter"));
    }
}
