package io.kestra.plugin.beckhoff.ads.integration;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.tasks.AdsRead;
import io.kestra.plugin.beckhoff.ads.tasks.AdsWrite;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.instanceOf;

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
        String localAmsNetId = System.getenv().getOrDefault("ADS_LOCAL_AMS_NET_ID", "");
        boolean autoAddRoute = Boolean.parseBoolean(System.getenv().getOrDefault("ADS_AUTO_ADD_ROUTE", "false"));
        String variable = System.getenv().getOrDefault("ADS_REAL_VARIABLE", "MAIN.nAdsKestraTest");
        String dataType = System.getenv().getOrDefault("ADS_REAL_DATATYPE", "DINT");

        RunContext runContext = runContextFactory.of(Map.of());

        AdsRead task = AdsRead.builder()
            .connection(
                AdsConnection.builder()
                    .targetIp(targetIp)
                    .targetAmsNetId(targetAmsNetId)
                    .targetAmsPort(targetAmsPort)
                    .localAmsNetId(localAmsNetId.isBlank() ? null : localAmsNetId)
                    .autoAddRoute(autoAddRoute)
                    .build()
            )
            .variable(Property.ofValue(variable))
            .dataType(Property.ofValue(dataType))
            .build();

        AdsRead.Output output = task.run(runContext);

        assertThat(output.getVariable(), is(variable));
        assertThat(output.getValue(), notNullValue());
        assertThat(output.getValue(), instanceOf(Number.class));

        long currentValue = ((Number) output.getValue()).longValue();
        long nextValue = currentValue + 1;

        AdsWrite writeTask = AdsWrite.builder()
            .connection(
                AdsConnection.builder()
                    .targetIp(targetIp)
                    .targetAmsNetId(targetAmsNetId)
                    .targetAmsPort(targetAmsPort)
                    .localAmsNetId(localAmsNetId.isBlank() ? null : localAmsNetId)
                    .autoAddRoute(autoAddRoute)
                    .build()
            )
            .variable(Property.ofValue(variable))
            .value(Property.ofValue(nextValue))
            .dataType(Property.ofValue(dataType))
            .build();

        AdsWrite.Output writeOutput = writeTask.run(runContext);
        assertThat(writeOutput.isWritten(), is(true));

        AdsRead.Output verifyOutput = task.run(runContext);
        assertThat(((Number) verifyOutput.getValue()).longValue(), is(nextValue));
    }
}
