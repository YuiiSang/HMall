package com.hmall.item.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Title: ElasticSearchConfig
 * @Author Tanght 363993584@qq.com
 * @Date 2025/9/14 20:19
 * @description:
 */
@Configuration
public class ElasticSearchConfig {
    private static final String HOST = "http://localhost:9200";
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create(HOST)));
    }

}
