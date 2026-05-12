package io.kestra.plugin.beckhoff.ads.integration;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.tasks.AdsRead;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@KestraTest
@EnabledIfEnvironmentVariable(named = "ADS_REAL_SMOKE_TEST", matches = "(?i)true")
class AdsRealServerSmokeTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void readFromRealAdsServer() throws Exception {
        String targetIp = System.getenv().getOrDefault("ADS_TARGET_IP", "localhost");
        String targetAmsNetId = System.getenv().getOrDefault("ADS_TARGET_AMS_NET_ID", "199.4.42.250.1.1");
        int targetAmsPort = Integer.parseInt(System.getenv().getOrDefault("ADS_TARGET_AMS_PORT", "851"));
        String variable = System.getenv().getOrDefault("ADS_REAL_VARIABLE", "GVL.Counter");
        String dataType = System.getenv().getOrDefault("ADS_REAL_DATATYPE", "DINT");

        RunContext runContext = runContextFactory.of(Map.of());

        AdsRead task = AdsRead.builder()
            .connection(
                AdsConnection.builder()
                    .targetIp(targetIp)
                    .targetAmsNetId(targetAmsNetId)
                    .targetAmsPort(targetAmsPort)
                    .build()
            )
            .variable(Property.ofValue(variable))
            .dataType(Property.ofValue(dataType))
            .build();

        AdsRead.Output output = task.run(runContext);

        assertThat(output.getVariable(), is(variable));
        assertThat(output.getValue(), notNullValue());
    }
}
