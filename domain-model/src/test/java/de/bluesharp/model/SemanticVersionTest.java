package de.bluesharp.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SemanticVersionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesVersionWithLeadingVPrefixSameAsWithout() throws Exception {
        var withPrefix = objectMapper.readValue("\"v1.2.4\"", SemanticVersion.class);
        var withoutPrefix = objectMapper.readValue("\"1.2.4\"", SemanticVersion.class);

        assertThat(withPrefix).isEqualTo(withoutPrefix);
    }

    @Test
    void ordersByMajorThenMinorThenPatch() throws Exception {
        var v100 = objectMapper.readValue("\"1.0.0\"", SemanticVersion.class);
        var v110 = objectMapper.readValue("\"1.1.0\"", SemanticVersion.class);
        var v200 = objectMapper.readValue("\"2.0.0\"", SemanticVersion.class);

        assertThat(v100).isLessThan(v110);
        assertThat(v110).isLessThan(v200);
        assertThat(v100.compareTo(v100)).isZero();
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> objectMapper.readValue("\"not-a-version\"", SemanticVersion.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> objectMapper.readValue("\"1.2\"", SemanticVersion.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void roundTripsThroughJacksonAsAPlainString() throws Exception {
        var version = objectMapper.readValue("\"1.2.4\"", SemanticVersion.class);

        var json = objectMapper.writeValueAsString(version);

        assertThat(json).isEqualTo("\"1.2.4\"");
        assertThat(objectMapper.readValue(json, SemanticVersion.class)).isEqualTo(version);
    }
}
