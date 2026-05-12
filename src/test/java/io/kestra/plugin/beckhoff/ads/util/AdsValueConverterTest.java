package io.kestra.plugin.beckhoff.ads.util;

import io.kestra.plugin.beckhoff.ads.model.AdsDataType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AdsValueConverterTest {
    @Test
    void convertsLong() {
        Object value = AdsValueConverter.convertInput("42", AdsDataType.DINT);
        assertThat(value, is(42L));
    }

    @Test
    void convertsBoolean() {
        Object value = AdsValueConverter.convertInput("true", AdsDataType.BOOL);
        assertThat(value, is(true));
    }
}
