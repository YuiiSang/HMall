package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;

/**
 * @Title: OrderDelayMsgListener
 * @Author Tanght 363993584@qq.com
 * @Date 2025/8/7 19:51
 * @description:
 */
@Configuration
@RequiredArgsConstructor
public class OrderDelayMsgListener {
    private final IOrderService orderService;
    private final PayClient payClient;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME, delayed = "true"),
            key = MQConstants.DELAY_ORDER_KEY

    ))
    public void listen(Long orderId) {
        // 查询订单
        Order order = orderService.getById(orderId);
        // 检测订单状态 判断是否已经支付
        if (order == null || order.getStatus() != 1) {
            return;
        }
        // 未支付 支付流水
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
        if (payOrderDTO != null && payOrderDTO.getStatus() == 3) {
            orderService.markOrderPaySuccess(orderId);
        } else {
            orderService.cancelOrder(orderId);
        }


        // 未支付 取消订单

    }
}
