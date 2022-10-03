/*
 * Copyright (c) 2004-2022 by Gigamon Systems, Inc. All Rights Reserved.
 */
package com.kafka.streamDataSource;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gjayaraman
 * Sep 18, 2022
 */
@Configuration
public class SpringElasticConfigurations
{

    @Value("${elasticsearch.target.index}")
    public String TARGET_INDEX;

    @Value("${elasticsearch.host}")
    private String elasticSearchHost;

    @Value("${elasticsearch.port}")
    private int elasticSearchPort;

    @Bean
    RestClient restClient() {
        return RestClient.builder(
                new HttpHost(elasticSearchHost, elasticSearchPort)).build();
    }

    @Bean
    RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClientBuilder(restClient())
                .setApiCompatibilityMode(true)
                .build();
    }

    public Listener bulkProcessorListener() {
        return new Listener()
        {
            @Override
            public void beforeBulk(final long executionId, final BulkRequest request) {
                //
            }

            @Override
            public void afterBulk(final long executionId, final BulkRequest request, final BulkResponse response) {
                System.out.println("Ingested Bulk Request. Execution Id : " + executionId + ", Bulk Request : " + request + ", Bulk Response : " + response);
            }

            @Override
            public void afterBulk(final long executionId, final BulkRequest request, final Throwable failure) {
                System.out.println("Exception while ingesting Bulk Request. Execution Id : " + executionId + ", Bulk Request : " + request + ", Error : " + failure);
            }
        };
    }

    @Bean
    public BulkProcessor bulkProcessor() {
        return BulkProcessor.builder(
                (request, bulkListener) ->
                        restHighLevelClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                bulkProcessorListener(), "target-elastic-index-processor").setBulkActions(10000).setBulkSize(ByteSizeValue.ofMb(50)).setConcurrentRequests(1).setFlushInterval(TimeValue.timeValueSeconds(10)).build();
    }
}
