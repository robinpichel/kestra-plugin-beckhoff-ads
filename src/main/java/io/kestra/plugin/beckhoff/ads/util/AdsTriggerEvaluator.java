package io.kestra.plugin.beckhoff.ads.util;

import io.kestra.plugin.beckhoff.ads.model.AdsException;

public final class AdsTriggerEvaluator {
    private AdsTriggerEvaluator() {
    }

    public static boolean shouldTrigger(String modeValue, Object previous, Object current, String thresholdValue) {
        if ("ON_CHANGE".equals(modeValue)) {
            return previous != null && !previous.equals(current);
        }

        if (thresholdValue == null || current == null) {
            return false;
        }

        double currentNumber = parseDouble(current, "current value");
        double thresholdNumber = parseDouble(thresholdValue, "threshold value");

        return switch (modeValue) {
            case "GT" -> currentNumber > thresholdNumber;
            case "LT" -> currentNumber < thresholdNumber;
            case "EQ" -> currentNumber == thresholdNumber;
            default -> false;
        };
    }

    private static double parseDouble(Object value, String label) {
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new AdsException("Unable to parse " + label + " as number: " + value, e);
        }
    }
}
