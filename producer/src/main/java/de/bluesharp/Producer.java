package de.bluesharp;

import de.bluesharp.kafka.KafkaAggregateOneTestDataProducer;
import de.bluesharp.kafka.KafkaAggregateTwoTestDataProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class Producer {
    public static void main(String[] args) {
        SpringApplication.run(Producer.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            @Autowired KafkaAggregateOneTestDataProducer p1,
            @Autowired KafkaAggregateTwoTestDataProducer p2
    ) {
        return args -> {
            var c1 = CompletableFuture.runAsync(p1);
            var c2 = CompletableFuture.runAsync(p2);
            CompletableFuture.allOf(c1, c2).join();
        };
    }
}