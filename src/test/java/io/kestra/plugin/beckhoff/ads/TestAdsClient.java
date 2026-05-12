package io.kestra.plugin.beckhoff.ads;

import io.kestra.plugin.beckhoff.ads.client.AdsClient;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import io.kestra.plugin.beckhoff.ads.model.AdsSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestAdsClient implements AdsClient {
    private final Map<String, Object> values = new ConcurrentHashMap<>();
    private final List<AdsSymbol> symbols = new ArrayList<>();

    public void setValue(String variable, Object value) {
        this.values.put(variable, value);
    }

    public void setSymbols(List<AdsSymbol> entries) {
        this.symbols.clear();
        this.symbols.addAll(entries);
    }

    @Override
    public Object read(AdsConnection connection, String variable, AdsDataType dataType) {
        return values.get(variable);
    }

    @Override
    public void write(AdsConnection connection, String variable, AdsDataType dataType, Object value) {
        values.put(variable, value);
    }

    @Override
    public List<AdsSymbol> discoverSymbols(AdsConnection connection) {
        return List.copyOf(symbols);
    }
}
