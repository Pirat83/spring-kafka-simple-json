package de.bluesharp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayloadStats {

    private long sum;
    private long count;
    private long min;
    private long max;

    public static PayloadStats empty() {
        return new PayloadStats(0L, 0L, Long.MAX_VALUE, Long.MIN_VALUE);
    }

    public PayloadStats add(long payload) {
        sum += payload;
        count++;
        min = Math.min(min, payload);
        max = Math.max(max, payload);
        return this;
    }
}
