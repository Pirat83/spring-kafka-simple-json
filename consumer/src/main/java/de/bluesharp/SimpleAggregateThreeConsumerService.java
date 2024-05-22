package de.bluesharp;

import de.bluesharp.model.AggregateFour;
import de.bluesharp.model.AggregateThree;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleAggregateThreeConsumerService {

    @KafkaListener(topics = "de.bluesharp.model.AggregateOne")
    public void listen(AggregateThree aggregate) {
        log.info("{}", aggregate);
    }


    @KafkaListener(topics = "de.bluesharp.model.AggregateTwo")
    public void listen(AggregateFour aggregate) {
        log.info("{}", aggregate);
    }
}
