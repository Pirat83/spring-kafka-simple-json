package de.bluesharp.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.regex.Pattern;

class SemanticVersionDeserializer extends JsonDeserializer<SemanticVersion> {

    private static final Pattern PATTERN = Pattern.compile("^[vV]?(\\d+)\\.(\\d+)\\.(\\d+)$");

    @Override
    public SemanticVersion deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        var value = parser.getValueAsString();
        var matcher = PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a valid semantic version: " + value);
        }
        var version = new SemanticVersion();
        version.setMajor(Integer.parseInt(matcher.group(1)));
        version.setMinor(Integer.parseInt(matcher.group(2)));
        version.setPatch(Integer.parseInt(matcher.group(3)));
        return version;
    }
}
