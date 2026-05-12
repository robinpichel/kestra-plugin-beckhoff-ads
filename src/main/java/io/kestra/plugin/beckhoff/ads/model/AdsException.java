package io.kestra.plugin.beckhoff.ads.model;

public class AdsException extends RuntimeException {
    public AdsException(String message) {
        super(message);
    }

    public AdsException(String message, Throwable cause) {
        super(message, cause);
    }
}
