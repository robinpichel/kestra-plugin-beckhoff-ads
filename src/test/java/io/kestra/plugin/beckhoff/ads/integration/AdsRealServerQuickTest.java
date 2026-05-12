package io.kestra.plugin.beckhoff.ads.integration;

import io.kestra.plugin.beckhoff.ads.client.OfficialAdsToJavaClient;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@EnabledIfEnvironmentVariable(named = "ADS_REAL_SMOKE_TEST", matches = "(?i)true")
class AdsRealServerQuickTest {
    @Test
    void readWriteReadbackOnRealAdsServer() throws Exception {
        String targetIp = System.getenv().getOrDefault("ADS_TARGET_IP", "localhost");
        String targetAmsNetId = System.getenv().getOrDefault("ADS_TARGET_AMS_NET_ID", "199.4.42.250.1.1");
        int targetAmsPort = Integer.parseInt(System.getenv().getOrDefault("ADS_TARGET_AMS_PORT", "851"));
        String localAmsNetId = System.getenv().getOrDefault("ADS_LOCAL_AMS_NET_ID", "");
        boolean autoAddRoute = Boolean.parseBoolean(System.getenv().getOrDefault("ADS_AUTO_ADD_ROUTE", "false"));
        String variable = System.getenv().getOrDefault("ADS_REAL_VARIABLE", "MAIN.nAdsKestraTest");

        AdsConnection connection = AdsConnection.builder()
            .targetIp(targetIp)
            .targetAmsNetId(targetAmsNetId)
            .targetAmsPort(targetAmsPort)
            .localAmsNetId(localAmsNetId.isBlank() ? null : localAmsNetId)
            .autoAddRoute(autoAddRoute)
            .build();

        OfficialAdsToJavaClient client = new OfficialAdsToJavaClient();

        Object readValue = client.read(connection, variable, AdsDataType.DINT);
        assertThat(readValue, notNullValue());

        long currentValue = ((Number) readValue).longValue();
        long nextValue = currentValue + 1;

        client.write(connection, variable, AdsDataType.DINT, nextValue);

        Object verify = client.read(connection, variable, AdsDataType.DINT);
        assertThat(((Number) verify).longValue(), is(nextValue));
    }
}
