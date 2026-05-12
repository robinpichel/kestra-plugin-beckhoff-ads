package io.kestra.plugin.beckhoff.ads.client;

import de.beckhoff.jni.Convert;
import de.beckhoff.jni.JNIByteBuffer;
import de.beckhoff.jni.JNILong;
import de.beckhoff.jni.tcads.AdsCallDllFunction;
import de.beckhoff.jni.tcads.AmsAddr;
import de.beckhoff.jni.tcads.AmsNetId;
import io.kestra.plugin.beckhoff.ads.config.AdsConnection;
import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import io.kestra.plugin.beckhoff.ads.model.AdsException;
import io.kestra.plugin.beckhoff.ads.model.AdsSymbol;

import java.util.Collections;
import java.util.List;

public class OfficialAdsToJavaClient implements AdsClient {
    private static final int HANDLE_SIZE = Integer.BYTES;

    @Override
    public Object read(AdsConnection connection, String variable, AdsDataType dataType) throws Exception {
        long port = openPortAndPrepare(connection);
        try {
            AmsAddr address = toAddress(connection);
            int handle = acquireHandle(port, address, variable);
            try {
                return readByHandle(port, address, handle, dataType, connection.getStringReadLength());
            } finally {
                releaseHandle(port, address, handle);
            }
        } finally {
            closePort(port);
        }
    }

    @Override
    public void write(AdsConnection connection, String variable, AdsDataType dataType, Object value) throws Exception {
        long port = openPortAndPrepare(connection);
        try {
            AmsAddr address = toAddress(connection);
            int handle = acquireHandle(port, address, variable);
            try {
                byte[] payload = toAdsBytes(value, dataType);
                JNIByteBuffer buffer = new JNIByteBuffer(payload);
                long error = AdsCallDllFunction.adsSyncWriteReqEx(
                    port,
                    address,
                    AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
                    Integer.toUnsignedLong(handle),
                    payload.length,
                    buffer
                );
                assertOk(error, "Failed to write ADS value for variable '" + variable + "'");
            } finally {
                releaseHandle(port, address, handle);
            }
        } finally {
            closePort(port);
        }
    }

    @Override
    public List<AdsSymbol> discoverSymbols(AdsConnection connection) {
        return Collections.emptyList();
    }

    private long openPortAndPrepare(AdsConnection connection) {
        if (connection == null) {
            throw new AdsException("ADS connection config is required");
        }

        long port = AdsCallDllFunction.adsPortOpenEx();
        if (port <= 0) {
            throw new AdsException("Failed to open ADS port using AdsToJava");
        }

        if (connection.getTimeout() != null) {
            long timeoutError = AdsCallDllFunction.adsSyncSetTimeoutEx(port, connection.getTimeout().toMillis());
            assertOk(timeoutError, "Failed to set ADS timeout");
        }

        if (connection.getLocalAmsNetId() != null && !connection.getLocalAmsNetId().isBlank()) {
            AmsNetId localNetId = toNetId(connection.getLocalAmsNetId());
            long localAddressError = AdsCallDllFunction.adsSetLocalAddress(localNetId);
            assertOk(localAddressError, "Failed to set local AMS Net ID");
        }

        if (Boolean.TRUE.equals(connection.getAutoAddRoute()) &&
            connection.getTargetIp() != null &&
            !connection.getTargetIp().isBlank()) {
            AmsNetId target = toNetId(connection.getTargetAmsNetId());
            long routeError = AdsCallDllFunction.adsAddLocalRoute(target, connection.getTargetIp());
            if (routeError != AdsCallDllFunction.ADSERR_NO_ERR) {
                // Ignore existing-route errors and continue.
                if (routeError != AdsCallDllFunction.ROUTERERR_PORTALREADYINUSE) {
                    throw new AdsException("Failed to add local ADS route, error code: " + routeError);
                }
            }
        }

        return port;
    }

    private void closePort(long port) {
        if (port > 0) {
            AdsCallDllFunction.adsPortCloseEx(port);
        }
    }

    private AmsAddr toAddress(AdsConnection connection) {
        if (connection.getTargetAmsNetId() == null || connection.getTargetAmsNetId().isBlank()) {
            throw new AdsException("targetAmsNetId is required");
        }

        AmsAddr address = new AmsAddr();
        address.setNetId(toNetId(connection.getTargetAmsNetId()));
        address.setPort(connection.getTargetAmsPort());
        return address;
    }

    private AmsNetId toNetId(String netIdValue) {
        AmsAddr addr = new AmsAddr();
        try {
            addr.setNetIdStringEx(netIdValue);
            return addr.getNetId();
        } catch (IllegalArgumentException e) {
            throw new AdsException("Invalid AMS Net ID format: " + netIdValue, e);
        }
    }

    private int acquireHandle(long port, AmsAddr address, String variable) {
        byte[] variableBytes = Convert.StringToByteArr(variable, true);
        JNIByteBuffer readBuffer = new JNIByteBuffer(HANDLE_SIZE);
        JNIByteBuffer writeBuffer = new JNIByteBuffer(variableBytes);
        JNILong bytesRead = new JNILong();

        long error = AdsCallDllFunction.adsSyncReadWriteReqEx2(
            port,
            address,
            AdsCallDllFunction.ADSIGRP_SYM_HNDBYNAME,
            0,
            HANDLE_SIZE,
            readBuffer,
            variableBytes.length,
            writeBuffer,
            bytesRead
        );
        assertOk(error, "Failed to resolve ADS handle for variable '" + variable + "'");

        return Convert.ByteArrToInt(readBuffer.getByteArray());
    }

    private void releaseHandle(long port, AmsAddr address, int handle) {
        JNIByteBuffer releaseBuffer = new JNIByteBuffer(Convert.IntToByteArr(handle));
        long error = AdsCallDllFunction.adsSyncWriteReqEx(
            port,
            address,
            AdsCallDllFunction.ADSIGRP_SYM_RELEASEHND,
            0,
            HANDLE_SIZE,
            releaseBuffer
        );
        assertOk(error, "Failed to release ADS handle");
    }

    private Object readByHandle(long port, AmsAddr address, int handle, AdsDataType type, Integer stringReadLength) {
        int readLength = expectedLength(type, stringReadLength);
        JNIByteBuffer data = new JNIByteBuffer(readLength);
        JNILong bytesRead = new JNILong();

        long error = AdsCallDllFunction.adsSyncReadReqEx2(
            port,
            address,
            AdsCallDllFunction.ADSIGRP_SYM_VALBYHND,
            Integer.toUnsignedLong(handle),
            readLength,
            data,
            bytesRead
        );
        assertOk(error, "Failed to read ADS value");

        byte[] payload = data.getByteArray();
        return fromAdsBytes(payload, type);
    }

    private int expectedLength(AdsDataType type, Integer stringReadLength) {
        return switch (type) {
            case BOOL -> 1;
            case INT -> Short.BYTES;
            case DINT -> Integer.BYTES;
            case REAL -> Float.BYTES;
            case LREAL -> Double.BYTES;
            case STRING -> (stringReadLength == null || stringReadLength <= 0) ? 256 : stringReadLength;
        };
    }

    private byte[] toAdsBytes(Object value, AdsDataType type) {
        if (value == null) {
            throw new AdsException("Cannot write null value to ADS");
        }

        return switch (type) {
            case BOOL -> Convert.BoolToByteArr((Boolean) value);
            case INT -> Convert.ShortToByteArr(((Number) value).shortValue());
            case DINT -> Convert.IntToByteArr(((Number) value).intValue());
            case REAL -> Convert.FloatToByteArr(((Number) value).floatValue());
            case LREAL -> Convert.DoubleToByteArr(((Number) value).doubleValue());
            case STRING -> Convert.StringToByteArr(String.valueOf(value), true);
        };
    }

    private Object fromAdsBytes(byte[] value, AdsDataType type) {
        return switch (type) {
            case BOOL -> Convert.ByteArrToBool(new byte[] {value[0]});
            case INT -> Convert.ByteArrToShort(copyFixed(value, Short.BYTES));
            case DINT -> Convert.ByteArrToInt(copyFixed(value, Integer.BYTES));
            case REAL -> Convert.ByteArrToFloat(copyFixed(value, Float.BYTES));
            case LREAL -> Convert.ByteArrToDouble(copyFixed(value, Double.BYTES));
            case STRING -> Convert.ByteArrToString(value);
        };
    }

    private byte[] copyFixed(byte[] source, int length) {
        byte[] out = new byte[length];
        System.arraycopy(source, 0, out, 0, Math.min(source.length, length));
        return out;
    }

    private void assertOk(long errorCode, String message) {
        if (errorCode != AdsCallDllFunction.ADSERR_NO_ERR) {
            throw new AdsException(message + ", ADS error code: " + errorCode);
        }
    }
}
