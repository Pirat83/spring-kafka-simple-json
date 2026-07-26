package de.bluesharp.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

class SemanticVersionSerializer extends JsonSerializer<SemanticVersion> {

    @Override
    public void serialize(SemanticVersion version, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(version.getMajor() + "." + version.getMinor() + "." + version.getPatch());
    }
}
