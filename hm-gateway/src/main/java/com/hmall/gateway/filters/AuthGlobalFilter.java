package com.hmall.gateway.filters;

import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @Title: AuthGlobalFilter
 * @Author Tanght 363993584@qq.com
 * @Date 2025/8/2 17:11
 * @description:
 */
@Component
@RequiredArgsConstructor

public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties properties;
    private final JwtTool jwtTool;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isExclude(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        String token = null;
        List<String> authorization = request.getHeaders().get("authorization");
        if (authorization != null && !authorization.isEmpty()) {
            token = authorization.get(0);
            System.out.println("token = " + token);
        }
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            // 拦截
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete(); // 结束请求
        }
        String userInfo = userId.toString();
        ServerWebExchange build = exchange.mutate().request(
                builder -> builder.header("user-info", userInfo)
        ).build();
        // 放行
        return chain.filter(build);
    }

    private boolean isExclude(String path) {
        for (String excludePath : properties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
