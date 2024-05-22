package de.bluesharp.kafka;

import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;

@Component
public class TopicMappingDefaultKafkaConsumerFactoryCustomizer implements DefaultKafkaConsumerFactoryCustomizer {

    @Override
    public void customize(DefaultKafkaConsumerFactory<?, ?> consumerFactory) {
        consumerFactory.getValueDeserializer();
    }
}
