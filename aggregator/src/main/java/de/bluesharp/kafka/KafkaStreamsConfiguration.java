package de.bluesharp.kafka;

import de.bluesharp.model.AggregateFour;
import de.bluesharp.model.DailyPayloadAggregate;
import de.bluesharp.model.PayloadStats;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfiguration {

    public static final String INPUT_TOPIC = "de.bluesharp.model.AggregateTwo";
    public static final String OUTPUT_TOPIC = DailyPayloadAggregate.class.getCanonicalName();

    @Bean
    public NewTopic dailyPayloadAggregate() {
        return TopicBuilder.name(OUTPUT_TOPIC)
                .partitions(1)
                .compact()
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip")
                .build();
    }

    @Bean
    public KStream<String, AggregateFour> dailyAggregateStream(StreamsBuilder builder) {
        JsonSerde<AggregateFour> inputSerde = new JsonSerde<>(AggregateFour.class).ignoreTypeHeaders();
        JsonSerde<PayloadStats> statsSerde = new JsonSerde<>(PayloadStats.class);
        JsonSerde<DailyPayloadAggregate> outputSerde = new JsonSerde<>(DailyPayloadAggregate.class);

        KStream<String, AggregateFour> stream =
                builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), inputSerde));

        stream.groupByKey(Grouped.with(Serdes.String(), inputSerde))
                // 1-day grace so out-of-order records (the producer emits timestamps in random
                // order across the last 24h) still land in their day's window instead of being
                // dropped as late once stream time advances to "now".
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofDays(1), Duration.ofDays(1)))
                .aggregate(
                        PayloadStats::empty,
                        (key, value, aggregate) -> aggregate.add(value.getPayload()),
                        Materialized.with(Serdes.String(), statsSerde)
                )
                .toStream()
                .map((windowedKey, stats) -> {
                    String uuid = windowedKey.key();
                    LocalDate day = windowedKey.window().startTime().atZone(ZoneOffset.UTC).toLocalDate();
                    return KeyValue.pair(uuid, DailyPayloadAggregate.of(day, stats));
                })
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), outputSerde));

        return stream;
    }
}
