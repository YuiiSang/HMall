//package com.hmall.gateway.routers;
//
//import cn.hutool.json.JSONUtil;
//import com.alibaba.cloud.nacos.NacosConfigManager;
//import com.alibaba.nacos.api.config.listener.Listener;
//import com.alibaba.nacos.api.exception.NacosException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.route.RouteDefinition;
//import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import javax.annotation.PostConstruct;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.Executor;
//
///**
// * @Title: DynamicRouterLoader
// * @Author Tanght 363993584@qq.com
// * @Date 2025/8/3 17:01
// * @description:
// */
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class DynamicRouterLoader {
//    private final NacosConfigManager nacosConfigManager;
//    private final RouteDefinitionWriter routeDefinitionWriter;
//    private final String dataId = "gateway-routes.json";
//    private final String group = "DEFAULT-GROUP";
//    private final Set<String> routeIds = new HashSet<>();
//    @PostConstruct
//    public void initRouteConfigListener() throws NacosException {
//        // 项目启动先拉取配置
//        nacosConfigManager.getConfigService()
//                .getConfigAndSignListener(dataId, group, 5000,
//                        new Listener() {
//                            @Override
//                            public Executor getExecutor() {
//                                return null;
//                            }
//
//                            @Override
//                            public void receiveConfigInfo(String s) {
//                                // 监听到配置变更 共享路由表
//                                log.info("路由配置变更:{}", s);
//                                updateConfig(s);
//                            }
//                        });
//        // 项目启动后 监听配置变更
//
//    }
//    public void updateConfig(String config) {
//        // 解析
//        List<RouteDefinition> list = JSONUtil.toList(config, RouteDefinition.class);
//        // 删除旧表
//        for (String routeId : routeIds) {
//            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
//        }
//        routeIds.clear();
//        for (RouteDefinition routeDefinition : list) {
//            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
//            // 记录
//            routeIds.add(routeDefinition.getId());
//        }
//    }
//}
