package io.kestra.plugin.beckhoff.ads.client;

import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import io.kestra.plugin.beckhoff.ads.model.AdsException;
import io.kestra.plugin.beckhoff.ads.model.AdsSymbol;

import java.util.List;

public final class AdsClientFactory {
    private static volatile Provider provider = new UnsupportedProvider();

    private AdsClientFactory() {
    }

    public static AdsClient getClient() {
        return provider.create();
    }

    public static void setProvider(Provider nextProvider) {
        provider = nextProvider;
    }

    public static void resetProvider() {
        provider = new UnsupportedProvider();
    }

    public interface Provider {
        AdsClient create();
    }

    private static class UnsupportedProvider implements Provider {
        @Override
        public AdsClient create() {
            return new UnsupportedClient();
        }
    }

    private static class UnsupportedClient implements AdsClient {
        @Override
        public Object read(AdsConnection connection, String variable, AdsDataType dataType) {
            throw new AdsException("No ADS client provider configured. Configure a Beckhoff ADS Java client adapter.");
        }

        @Override
        public void write(AdsConnection connection, String variable, AdsDataType dataType, Object value) {
            throw new AdsException("No ADS client provider configured. Configure a Beckhoff ADS Java client adapter.");
        }

        @Override
        public List<AdsSymbol> discoverSymbols(AdsConnection connection) {
            throw new AdsException("No ADS client provider configured. Configure a Beckhoff ADS Java client adapter.");
        }
    }
}
