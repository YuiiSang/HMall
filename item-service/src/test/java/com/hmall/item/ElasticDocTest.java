package com.hmall.item;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import io.lettuce.core.ScriptOutputType;
import org.apache.http.HttpHost;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

/**
 * @Title: com.hmall.item.ElasticTest
 * @Author Tanght 363993584@qq.com
 * @Date 2025/8/9 23:18
 * @description:
 */
@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticDocTest {
    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;


//    @Test
//    public void test(){
//
//    }
    @BeforeEach
    public void init() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://localhost:9200")
        ));
    }
    @AfterEach
    public void close() throws Exception {
        if (client != null) client.close();
    }

   @Test
   public void testIndexxDoc() throws IOException {
       IndexRequest request = new IndexRequest("items").id("1");
       request.source("{}", XContentType.JSON);
       client.index(request, RequestOptions.DEFAULT);
    }
    @Test
    public void testIndexDoc() throws IOException {
        Item item = itemService.getById(100000011127L);
        ItemDoc itemDoc = BeanUtils.copyBean(item, ItemDoc.class);
        IndexRequest request = new IndexRequest("items").id("100000011127");
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }
    @Test
    public void testGetDoc() throws IOException {
        GetRequest request = new GetRequest("items", "100000011127");
        GetResponse documentFields = client.get(request, RequestOptions.DEFAULT);
        System.out.println(documentFields.getSource());
    }
    @Test
    public void testUpdate(){
        Item item = itemService.getById(100000011127L);
        ItemDoc itemDoc = BeanUtils.copyBean(item, ItemDoc.class);
        UpdateRequest request = new UpdateRequest("items", "100000011127");
        request.doc(
                "price",25600
        );
        try {
            client.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testBulk() throws IOException {
        int pageNo = 1, pageSize = 500;

        while (true) {
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));
            List<Item> items = page.getRecords();
            if (items.isEmpty()) {
                return;
            }
            BulkRequest request = new BulkRequest();
            items.forEach(item -> {
                request.add(new IndexRequest("items")
                        .id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtils.copyProperties(item, ItemDoc.class)), XContentType.JSON));
            });
            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
        }

    }
    @Test
    public void testMatchAll() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source()
                .query(QueryBuilders.matchAllQuery());
        SearchResponse res = client.search(request, RequestOptions.DEFAULT);
        System.out.println(res);
        // 解析hits
        SearchHits hits = res.getHits();
        long total = hits.getTotalHits().value;
        SearchHit[] totalHits = hits.getHits();
        for (SearchHit hit : totalHits) {
            String json = hit.getSourceAsString();
            System.out.println( json);
        }

    }
    @Test
    public void testBool() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source()
                .query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("name", "脱脂牛奶"))
                        .filter(QueryBuilders.termQuery("brand","德亚"))
                        .filter(QueryBuilders.rangeQuery("price").lt(30000)));
        SearchResponse res = client.search(request, RequestOptions.DEFAULT);
        // 解析hits
        SearchHits hits = res.getHits();
        long total = hits.getTotalHits().value;
        SearchHit[] totalHits = hits.getHits();
        for (SearchHit hit : totalHits) {
            String json = hit.getSourceAsString();
            System.out.println( json);
        }
    }
    @Test
    public void testSortAndPage() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().from(0).size(5);
        request.source().sort("sold", SortOrder.DESC).sort("price",SortOrder.DESC);
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = search.getHits();
        long total = hits.getTotalHits().value;
        SearchHit[] totalHits = hits.getHits();
        for (SearchHit hit : totalHits) {
            String json = hit.getSourceAsString();
            System.out.println( json);
        }
        System.out.println(total);
    }
}
