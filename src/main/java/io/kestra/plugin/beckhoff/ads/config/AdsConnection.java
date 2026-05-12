package io.kestra.plugin.beckhoff.ads.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@Getter
@SuperBuilder
@NoArgsConstructor
public class AdsConnection {
    @Schema(title = "Target AMS Net ID", example = "5.32.176.1.1.1")
    private String targetAmsNetId;

    @Schema(title = "Target ADS Port", example = "851")
    @Builder.Default
    private Integer targetAmsPort = 851;

    @Schema(title = "Read/write timeout", example = "PT5S")
    @Builder.Default
    private Duration timeout = Duration.ofSeconds(5);
}
