package io.kestra.plugin.beckhoff.ads.util;

import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import io.kestra.plugin.beckhoff.ads.model.AdsException;

public final class AdsValueConverter {
    private AdsValueConverter() {
    }

    public static Object convertInput(Object value, AdsDataType type) {
        if (value == null) {
            return null;
        }

        try {
            return switch (type) {
                case BOOL -> {
                    if (value instanceof Boolean b) {
                        yield b;
                    }
                    yield Boolean.parseBoolean(value.toString());
                }
                case INT -> Integer.parseInt(value.toString());
                case DINT -> Long.parseLong(value.toString());
                case REAL -> Float.parseFloat(value.toString());
                case LREAL -> Double.parseDouble(value.toString());
                case STRING -> value.toString();
            };
        } catch (RuntimeException e) {
            throw new AdsException("Unable to convert value '" + value + "' to ADS type " + type, e);
        }
    }
}
