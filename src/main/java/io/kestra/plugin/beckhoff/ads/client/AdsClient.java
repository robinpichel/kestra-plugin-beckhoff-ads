package io.kestra.plugin.beckhoff.ads.client;

import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import io.kestra.plugin.beckhoff.ads.model.AdsSymbol;

import java.util.List;

public interface AdsClient {
    Object read(AdsConnection connection, String variable, AdsDataType dataType) throws Exception;

    void write(AdsConnection connection, String variable, AdsDataType dataType, Object value) throws Exception;

    List<AdsSymbol> discoverSymbols(AdsConnection connection) throws Exception;
}
