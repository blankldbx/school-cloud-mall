package com.xidian.cloud.mall.zuul.feign;

import com.xidian.cloud.mall.user.model.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author LDBX
 * UserFeignClient
 */
@FeignClient(value = "cloud-mall-user")
public interface UserFeignClient {

    @PostMapping("/checkAdminRole")
    public boolean checkAdminRole(@RequestBody User user);
}
