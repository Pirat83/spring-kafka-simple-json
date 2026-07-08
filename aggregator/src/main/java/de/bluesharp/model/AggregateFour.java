package de.bluesharp.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AggregateFour {

    private UUID uuid;
    private Instant timestamp;
    private Long payload;
}
