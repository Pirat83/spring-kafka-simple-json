package de.bluesharp.model;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AggregateOne {


    private UUID uuid;
    private Instant timestamp;
    private String payload;
}
