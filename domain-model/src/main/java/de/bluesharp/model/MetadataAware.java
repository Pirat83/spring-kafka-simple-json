package de.bluesharp.model;

import java.time.Instant;
import java.util.UUID;

public interface MetadataAware {
    UUID getUuid();

    Instant getTimestamp();
}
