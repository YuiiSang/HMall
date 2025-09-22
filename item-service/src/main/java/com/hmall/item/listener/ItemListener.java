package com.hmall.item.listener;

import com.hmall.api.dto.ItemDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.constants.MQConstants;
import com.hmall.item.domain.ItemMQDto;
import com.hmall.item.domain.po.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Title: ItemListenter
 * @Author Tanght 363993584@qq.com
 * @Date 2025/9/14 20:33
 * @description:
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ItemListener {

    private final RestHighLevelClient client;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MQConstants.ITEM_SEARCH_QUEUE_NAME),
                    exchange = @Exchange(name = MQConstants.ITEM_EXCHANGE_NAME),
                    key = MQConstants.ITEM_QUERY_KEY
            ))
    public void ItemMessageListener(ItemMQDto itemMQDto) {
        if (itemMQDto.getItemDTO().getId() == null) return;
        switch (itemMQDto.getOperate()) {
            case ADD -> addItemByIndex(itemMQDto.getItemDTO());
            case UPDATE -> updateItemByIndex(itemMQDto.getItemDTO());
            case DELETE -> deleteItemByIndex(itemMQDto.getItemDTO().getId());
            default -> log.error("未知操作类型");
        }
    }
    private void addItemByIndex(ItemDTO itemDTO) {

    }
    private void updateItemByIndex(ItemDTO itemDTO) {

    }
    private void deleteItemByIndex(Long itemId) {

    }

}
