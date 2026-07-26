package de.bluesharp.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize(using = SemanticVersionSerializer.class)
@JsonDeserialize(using = SemanticVersionDeserializer.class)
public class SemanticVersion implements Comparable<SemanticVersion> {

    private int major;
    private int minor;
    private int patch;

    @Override
    public int compareTo(SemanticVersion other) {
        var result = Integer.compare(major, other.major);
        if (result == 0) {
            result = Integer.compare(minor, other.minor);
        }
        if (result == 0) {
            result = Integer.compare(patch, other.patch);
        }
        return result;
    }
}
