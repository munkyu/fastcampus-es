package com.fastcampus.indexer.helper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
@Slf4j
public class ESHelper {
    static void DoBulk(ElasticsearchClient elasticsearchClient, BulkRequest.Builder br) throws IOException {
        BulkResponse result = elasticsearchClient.bulk(br.build());
        if (result.errors()) {
            log.error("Bulk had errors");
            for (BulkResponseItem item: result.items()) {
                if (item.error() != null) {
                    log.error(item.error().reason());
                }
            }
        }
    }
}
