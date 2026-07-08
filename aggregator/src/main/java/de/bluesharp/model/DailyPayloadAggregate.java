package de.bluesharp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPayloadAggregate {

    private LocalDate day;
    private long sumPayload;
    private long count;
    private long min;
    private long max;
    private double avg;

    public static DailyPayloadAggregate of(LocalDate day, PayloadStats stats) {
        double avg = stats.getCount() == 0 ? 0.0 : (double) stats.getSum() / stats.getCount();
        return new DailyPayloadAggregate(
                day,
                stats.getSum(),
                stats.getCount(),
                stats.getMin(),
                stats.getMax(),
                avg
        );
    }
}
