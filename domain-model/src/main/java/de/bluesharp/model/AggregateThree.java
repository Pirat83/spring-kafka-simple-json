package de.bluesharp.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AggregateThree implements MetadataAware {


    private UUID uuid;
    private Instant timestamp;
    private String payload;
}
