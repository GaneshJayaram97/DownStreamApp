/*
 * Copyright (c) 2004-2022 by Gigamon Systems, Inc. All Rights Reserved.
 */
package com.kafka.streamDataSource;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

/**
 * @author gjayaraman
 * Sep 22, 2022
 */
@Component
public class StreamDataSource
{
    @Autowired
    SpringElasticConfigurations springElasticConfigurations;
    @Value("${kafka.topic}")
    String topic;

    public boolean isElasticReady() {
        try {
            ClusterHealthRequest request = new ClusterHealthRequest();
            request.waitForYellowStatus();
            ClusterHealthResponse response = springElasticConfigurations.restHighLevelClient().cluster().health(request, RequestOptions.DEFAULT);
            return !response.isTimedOut() && !ClusterHealthStatus.RED.name().equalsIgnoreCase(response.getStatus().name());
        }
        catch (Exception ex) {
            System.out.println("Exception while checking the ES readiness : " + ex);
            return false;
        }
    }

    public void assertElasticReady() throws InterruptedException {
        while (!isElasticReady()) {
            System.out.println("ES is not up and running or not ready. Retrying after a second");
            Thread.sleep(1000);
        }
    }

    @Autowired
    public void process(StreamsBuilder streamsBuilder) throws InterruptedException {

        assertElasticReady();
        String target_index = springElasticConfigurations.TARGET_INDEX;

        System.out.println("Processing the stream and topic : " + topic);
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Object> objectSerde = Serdes.serdeFrom(new JsonSerializer<Object>(), new JsonDeserializer<Object>());

        streamsBuilder.build();
        KStream<String, Object> streamData = streamsBuilder.stream(topic, Consumed.with(stringSerde, objectSerde));
        streamData.map((key, value) -> {
            System.out.println("Ingesting Data : " + value + " into index : " + target_index);
            Map<String, Object> map = (Map<String, Object>) value;
            springElasticConfigurations.bulkProcessor().add(new IndexRequest(target_index).source(map, XContentType.JSON));
            return new KeyValue<>(key, value);
        });
    }
}
