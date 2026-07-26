package de.bluesharp.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AggregateTwo implements MetadataAware<SemanticVersion> {


    private UUID uuid;
    private Instant timestamp;
    private Long payload;
    private SemanticVersion version;
}
