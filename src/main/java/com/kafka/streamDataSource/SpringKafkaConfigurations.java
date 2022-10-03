/*
 * Copyright (c) 2004-2022 by Gigamon Systems, Inc. All Rights Reserved.
 */
package com.kafka.streamDataSource;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * @author gjayaraman
 * Sep 18, 2022
 */
@Configuration
@EnableKafka
@EnableKafkaStreams
public class SpringKafkaConfigurations
{

    @Value("${kafka.topic}")
    public String TOPIC;

    @Value("${kafka.bootstrap.server}")
    private String BOOTSTRAP_ADDRESS;

    public Map<String, Object> getDefaultProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_ADDRESS);
        return props;
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    @Primary
    KafkaStreamsConfiguration kStreamsConfig() {
        System.out.println("Server Address : " + BOOTSTRAP_ADDRESS + " Topic : " + TOPIC);
        Map<String, Object> props = getDefaultProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-app");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.serdeFrom(new JsonSerializer<Object>(), new JsonDeserializer<Object>()).getClass().getName());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        props.put(JsonDeserializer.KEY_DEFAULT_TYPE, String.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, JsonSerde.class);
        return new KafkaStreamsConfiguration(props);
    }
}
