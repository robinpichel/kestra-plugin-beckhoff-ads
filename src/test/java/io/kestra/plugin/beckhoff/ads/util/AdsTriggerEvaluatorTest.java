package io.kestra.plugin.beckhoff.ads.util;

import io.kestra.plugin.beckhoff.ads.model.AdsException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdsTriggerEvaluatorTest {
    @Test
    void onChangeTriggersWhenValueChanges() {
        boolean result = AdsTriggerEvaluator.shouldTrigger("ON_CHANGE", 10, 11, null);
        assertThat(result, is(true));
    }

    @Test
    void onChangeDoesNotTriggerOnFirstPoll() {
        boolean result = AdsTriggerEvaluator.shouldTrigger("ON_CHANGE", null, 11, null);
        assertThat(result, is(false));
    }

    @Test
    void gtTriggersWhenAboveThreshold() {
        boolean result = AdsTriggerEvaluator.shouldTrigger("GT", null, 12, "10");
        assertThat(result, is(true));
    }

    @Test
    void ltTriggersWhenBelowThreshold() {
        boolean result = AdsTriggerEvaluator.shouldTrigger("LT", null, 8, "10");
        assertThat(result, is(true));
    }

    @Test
    void eqTriggersWhenEqualThreshold() {
        boolean result = AdsTriggerEvaluator.shouldTrigger("EQ", null, 10, "10");
        assertThat(result, is(true));
    }

    @Test
    void throwsWhenThresholdNotNumeric() {
        assertThrows(AdsException.class, () -> AdsTriggerEvaluator.shouldTrigger("GT", null, 10, "abc"));
    }
}
