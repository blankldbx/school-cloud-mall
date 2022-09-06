package com.xidian.cloud.mall.cartorder.feign;

import com.xidian.cloud.mall.user.model.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author LDBX
 * UserFeign客户端
 */
@FeignClient(value = "cloud-mall-user")
public interface UserFeignClient {

    /**
     * 获取当前登录的User对象
     * @return
     */
    @GetMapping("/getUser")
    User getUser();
}
