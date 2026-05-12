package io.kestra.plugin.beckhoff.ads.tasks;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.beckhoff.ads.TestAdsClient;
import io.kestra.plugin.beckhoff.ads.client.AdsClientFactory;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@KestraTest
class AdsReadTest {
    @Inject
    private RunContextFactory runContextFactory;

    private TestAdsClient testClient;

    @BeforeEach
    void setup() {
        testClient = new TestAdsClient();
        testClient.setValue("GVL.Counter", 42L);
        AdsClientFactory.setProvider(() -> testClient);
    }

    @AfterEach
    void cleanup() {
        AdsClientFactory.resetProvider();
    }

    @Test
    void run() throws Exception {
        RunContext runContext = runContextFactory.of(Map.of());

        AdsRead task = AdsRead.builder()
            .connection(AdsConnection.builder().targetAmsNetId("5.32.176.1.1.1").build())
            .variable(Property.ofValue("GVL.Counter"))
            .dataType(Property.ofValue("DINT"))
            .build();

        AdsRead.Output output = task.run(runContext);

        assertThat(output.getVariable(), is("GVL.Counter"));
        assertThat(output.getDataType(), is("DINT"));
        assertThat(output.getValue(), is(42L));
    }
}
