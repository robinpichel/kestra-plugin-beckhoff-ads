package io.kestra.plugin.beckhoff.ads.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdsSymbol {
    @Schema(title = "Symbol name", example = "GVL.Counter")
    private final String name;

    @Schema(title = "ADS type", example = "DINT")
    private final String type;

    @Schema(title = "Symbol size in bytes", example = "4")
    private final Integer size;
}
