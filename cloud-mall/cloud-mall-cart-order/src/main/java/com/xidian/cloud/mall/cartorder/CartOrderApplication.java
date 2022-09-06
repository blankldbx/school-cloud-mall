package com.xidian.cloud.mall.cartorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author LDBX
 * 购物车订单模块启动类
 */
@SpringBootApplication
@MapperScan(basePackages = "com.xidian.cloud.mall.cartorder.model.dao")
@EnableRedisHttpSession
@EnableFeignClients
public class CartOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartOrderApplication.class, args);
    }
}
