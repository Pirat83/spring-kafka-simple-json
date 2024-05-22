package de.bluesharp.kafka;

import de.bluesharp.model.AggregateOne;
import de.bluesharp.model.AggregateTwo;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ComponentScan
@RequiredArgsConstructor
public class KafkaConfiguration {

    @Bean
    public NewTopic aggregateOne() {
        return TopicBuilder.name(AggregateOne.class.getCanonicalName())
                .partitions(1)
                .compact()
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip")
                .build();
    }

    @Bean
    public NewTopic aggregateTwo() {
        return TopicBuilder.name(AggregateTwo.class.getCanonicalName())
                .partitions(1)
                .compact()
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip")
                .build();
    }
}
