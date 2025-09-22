package com.hmall.item.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.query.ItemPageQuery;
import com.hmall.item.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final IItemService itemService;

    private final RestHighLevelClient restHighLevelClient;

//    @ApiOperation("搜索商品")
//    @GetMapping("/list")
//    public PageDTO<ItemDTO> search(ItemPageQuery query) {
//        // 分页查询
//        Page<Item> result = itemService.lambdaQuery()
//                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
//                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
//                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
//                .eq(Item::getStatus, 1)
//                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
//                .page(query.toMpPage("update_time", false));
//        // 封装并返回
//        return PageDTO.of(result, ItemDTO.class);
//    }



    // 使用es重构搜索
    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) throws IOException {
        System.out.println(query.toString());
        /* 1. 构造 bool 查询 */
        BoolQueryBuilder bool = QueryBuilders.boolQuery(); // 只卖上架商品

        if (StrUtil.isNotBlank(query.getKey())) {
            bool.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        if (StrUtil.isNotBlank(query.getBrand())) {
            bool.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        if (StrUtil.isNotBlank(query.getCategory())) {
            bool.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }
        if (query.getMinPrice() != null || query.getMaxPrice() != null) {
            bool.filter(QueryBuilders.rangeQuery("price")
                    .gte(query.getMinPrice() == null ? 0 : query.getMinPrice())
                    .lte(query.getMaxPrice() == null ? Integer.MAX_VALUE : query.getMaxPrice()));
        }

        /* 2. 分页、排序 */
        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(bool)
                .from((query.getPageNo() - 1) * query.getPageSize())
                .size(query.getPageSize())
                .sort("updateTime", SortOrder.DESC);

        /* 3. 发请求 */
        SearchRequest request = new SearchRequest("items").source(source);
        SearchResponse resp = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        /* 4. 解析结果 */
        List<ItemDTO> records = new ArrayList<>();
        for (SearchHit hit : resp.getHits().getHits()) {
            ItemDTO dto = BeanUtil.toBean(hit.getSourceAsMap(), ItemDTO.class);
            dto.setId(Long.valueOf(hit.getId())); // _id 转 id
            records.add(dto);
        }

        /* 5. 封装分页对象 */
        long total = resp.getHits().getTotalHits().value;
        PageDTO<ItemDTO> dto = new PageDTO<>();
        dto.setTotal(total);
        dto.setPages((long) Math.ceil(total / (double) query.getPageSize()));
        dto.setList(records);
        return dto;
    }
}
