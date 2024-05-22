package de.bluesharp.kafka;

import de.bluesharp.model.AggregateOne;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.instancio.Select.field;

@Component
@RequiredArgsConstructor
public class KafkaAggregateOneTestDataProducer implements Runnable {

    private final KafkaTemplate<String, AggregateOne> kafkaTemplate;

    @Override
    public void run() {
        var model = Instancio.of(AggregateOne.class)
                .generate(field(AggregateOne::getTimestamp),
                        gen -> gen.temporal()
                                .instant()
                                .range(Instant.now().minus(1, ChronoUnit.DAYS), Instant.now())
                ).toModel();


        //noinspection DataFlowIssue
        Instancio.stream(model)
                .parallel()
                .forEach(i -> kafkaTemplate.send(
                        AggregateOne.class.getCanonicalName(), null, i.getTimestamp().toEpochMilli(),
                        i.getUuid().toString(), i
                ));
    }
}
