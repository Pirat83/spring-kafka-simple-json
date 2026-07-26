package de.bluesharp.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AggregateOne implements MetadataAware<Instant> {


    private UUID uuid;
    private Instant timestamp;
    private String payload;

    @Override
    public Instant getVersion() {
        return timestamp;
    }
}
