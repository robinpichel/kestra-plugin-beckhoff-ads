package io.kestra.plugin.beckhoff.ads.model;

public enum AdsDataType {
    BOOL,
    INT,
    DINT,
    REAL,
    LREAL,
    STRING;

    public static AdsDataType from(String value) {
        return AdsDataType.valueOf(value.toUpperCase());
    }
}
