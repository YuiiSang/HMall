package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Title: PayStatusListener
 * @Author Tanght 363993584@qq.com
 * @Date 2025/8/6 17:27
 * @description:
 */
@Component
@RequiredArgsConstructor
public class PayStatusListener {
    private final IOrderService orderService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"

    ))
    public void markOrderPaySuccess(Long orderId){
        // 查询订单
        Order order = orderService.getById(orderId);
        // 判断订单是否未支付
        if (order == null || order.getStatus() != 1) {
            return;
        }
        System.out.println("订单支付成功，订单号：" + orderId);
        orderService.markOrderPaySuccess(orderId);
    }
}
