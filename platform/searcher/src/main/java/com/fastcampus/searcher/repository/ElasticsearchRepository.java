package com.fastcampus.searcher.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fastcampus.searcher.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
@Slf4j
public class ElasticsearchRepository {
    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchRepository(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public SearchResponse<Product> searchProducts(String query, int size, Double lastScore, String lastId) throws IOException {
        FunctionScoreQuery functionScore = QueryBuilders.functionScore()
                .query(QueryBuilders.match()
                        .field("title")
                        .query(query).build()._toQuery())
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Replace).functions(
                        new FunctionScore.Builder()
                                .filter(QueryBuilders.match()
                                        .field("title")
                                        .query(query).build()._toQuery())
                                .weight(1.0)
                                .build(),
                        new FunctionScore.Builder()
                                .fieldValueFactor(new FieldValueFactorScoreFunction.Builder().field("stars").build())
                                .weight(0.1)
                                .build(),
                    new FunctionScore.Builder()
                            .filter(QueryBuilders.match()
                                    .field("is_recommend_seller")
                                    .query("true").build()._toQuery())
                            .weight(0.2)
                            .build()
                        )
                .build();

        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .query(functionScore._toQuery())
                .index("products")
                .size(size)
                .sort(List.of(
                        new SortOptions.Builder().field(new FieldSort.Builder().field("_score").order(SortOrder.Desc).build()).build(),
                        new SortOptions.Builder().field(new FieldSort.Builder().field("asin").order(SortOrder.Desc).build()).build()
                ));
        if  (lastId!= null && lastScore != null) {
            searchRequestBuilder.searchAfter(lastScore.toString(), lastId.toString());
        }

        SearchRequest searchRequest = searchRequestBuilder
                .build();


        log.info("searchRequest: {}", searchRequest);

        return elasticsearchClient.search(searchRequest, Product.class);

    }
}
