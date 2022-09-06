package com.xidian.cloud.mall.cartorder.service;

import com.github.pagehelper.PageInfo;
import com.xidian.cloud.mall.cartorder.model.request.CreateOrderRequest;
import com.xidian.cloud.mall.cartorder.model.vo.OrderVO;

/**
 * @author LDBX
 * 订单Service
 */
public interface OrderService {

    String create(CreateOrderRequest createOrderRequest);

    OrderVO detail(String orderNo);

    PageInfo listForCustomer(Integer pageNum, Integer pageSize);

    void cancel(String orderNo);

    String qrcode(String orderNo);

    void pay(String orderNo);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    void deliver(String orderNo);

    //订单完结
    void finish(String orderNo);
}
