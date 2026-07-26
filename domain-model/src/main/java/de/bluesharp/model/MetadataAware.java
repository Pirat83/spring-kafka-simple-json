package de.bluesharp.model;

import java.time.Instant;
import java.util.UUID;

public interface MetadataAware<V extends Comparable<V>> {
    UUID getUuid();

    Instant getTimestamp();

    V getVersion();
}
