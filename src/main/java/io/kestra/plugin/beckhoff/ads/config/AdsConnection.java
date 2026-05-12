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
    @Schema(title = "Target host/IP for route setup", example = "192.168.0.20")
    private String targetIp;

    @Schema(title = "Enable automatic adsAddLocalRoute call", example = "false")
    @Builder.Default
    private Boolean autoAddRoute = false;

    @Schema(title = "Target AMS Net ID", example = "5.32.176.1.1.1")
    private String targetAmsNetId;

    @Schema(title = "Optional local AMS Net ID", example = "192.168.0.10.1.1")
    private String localAmsNetId;

    @Schema(title = "Target ADS Port", example = "851")
    @Builder.Default
    private Integer targetAmsPort = 851;

    @Schema(title = "Read/write timeout", example = "PT5S")
    @Builder.Default
    private Duration timeout = Duration.ofSeconds(5);

    @Schema(title = "Read buffer size for STRING values", example = "256")
    @Builder.Default
    private Integer stringReadLength = 256;
}
