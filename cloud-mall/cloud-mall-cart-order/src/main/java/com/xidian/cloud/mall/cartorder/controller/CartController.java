package com.xidian.cloud.mall.cartorder.controller;


import com.xidian.cloud.mall.cartorder.feign.UserFeignClient;
import com.xidian.cloud.mall.cartorder.model.vo.CartVO;
import com.xidian.cloud.mall.cartorder.service.CartService;
import com.xidian.cloud.mall.common.ApiRestResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author LDBX
 * 购物车Controller
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @Autowired
    UserFeignClient userFeignClient;

    @GetMapping("/list")
    @ApiOperation("购物车列表")
    public ApiRestResponse list() {
        //内部获取用户ID，防止横向越权
        List<CartVO> cartList = cartService.list(userFeignClient.getUser().getId());
        return ApiRestResponse.success(cartList);
    }

    @PostMapping("/add")
    @ApiOperation("添加商品到购物车")
    public ApiRestResponse add(@RequestParam Integer productId, @RequestParam Integer count) {
        List<CartVO> cartVOList = cartService.add(userFeignClient.getUser().getId(), productId, count);
        return ApiRestResponse.success(cartVOList);
    }

    @PostMapping("/update")
    @ApiOperation("更新购物车")
    public ApiRestResponse update(@RequestParam Integer productId, @RequestParam Integer count) {
        List<CartVO> cartVOList = cartService.update(userFeignClient.getUser().getId(), productId, count);
        return ApiRestResponse.success(cartVOList);
    }

    @PostMapping("/delete")
    @ApiOperation("删除购物车")
    public ApiRestResponse delete(@RequestParam Integer productId) {
        //不能传入userId,cartId,否则可以删除别人的购物车
        List<CartVO> cartVOList = cartService.delete(userFeignClient.getUser().getId(), productId);
        return ApiRestResponse.success(cartVOList);
    }

    @PostMapping("/select")
    @ApiOperation("选中/不选中购物车")
    public ApiRestResponse select(@RequestParam Integer productId, @RequestParam Integer selected) {
        //不能传入userId,cartId,否则可以删除别人的购物车
        List<CartVO> cartVOList = cartService.selectOrNot(userFeignClient.getUser().getId(), productId,selected);
        return ApiRestResponse.success(cartVOList);
    }


    @PostMapping("/selectAll")
    @ApiOperation("全选中/全不选中购物车")
    public ApiRestResponse select(@RequestParam Integer selected) {
        //不能传入userId,cartId,否则可以删除别人的购物车
        List<CartVO> cartVOList = cartService.selectAllOrNot(userFeignClient.getUser().getId(), selected);
        return ApiRestResponse.success(cartVOList);
    }
}
