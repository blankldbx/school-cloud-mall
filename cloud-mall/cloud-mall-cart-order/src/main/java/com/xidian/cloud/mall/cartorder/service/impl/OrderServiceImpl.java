package com.xidian.cloud.mall.cartorder.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.zxing.WriterException;
import com.xidian.cloud.mall.cartorder.feign.ProductFeignClient;
import com.xidian.cloud.mall.cartorder.feign.UserFeignClient;
import com.xidian.cloud.mall.cartorder.model.dao.CartMapper;
import com.xidian.cloud.mall.cartorder.model.dao.OrderItemMapper;
import com.xidian.cloud.mall.cartorder.model.dao.OrderMapper;
import com.xidian.cloud.mall.cartorder.model.pojo.Order;
import com.xidian.cloud.mall.cartorder.model.pojo.OrderItem;
import com.xidian.cloud.mall.cartorder.model.request.CreateOrderRequest;
import com.xidian.cloud.mall.cartorder.model.vo.CartVO;
import com.xidian.cloud.mall.cartorder.model.vo.OrderItemVO;
import com.xidian.cloud.mall.cartorder.model.vo.OrderVO;
import com.xidian.cloud.mall.cartorder.service.CartService;
import com.xidian.cloud.mall.cartorder.service.OrderService;
import com.xidian.cloud.mall.cartorder.util.OrderCodeFactory;
import com.xidian.cloud.mall.categoryproduct.model.pojo.Product;
import com.xidian.cloud.mall.common.Constant;
import com.xidian.cloud.mall.exception.XidianMallException;
import com.xidian.cloud.mall.exception.XidianMallExceptionEnum;
import com.xidian.cloud.mall.util.QRCodeGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author LDBX
 * 订单Service实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartService cartService;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    CartMapper cartMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    UserFeignClient userFeignClient;

    @Value("${file.upload.ip}")
    String ip;

    @Value("${file.upload.port}")
    String port;

    @Value("${file.upload.dir}")
    String FILE_UPLOAD_DIR;

    //数据库事务
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String create(CreateOrderRequest createOrderRequest) {
        //拿到用户ID
        Integer userId = userFeignClient.getUser().getId();

        //从购物车查找已勾选的商品
        List<CartVO> cartVOList = cartService.list(userId);
        ArrayList<CartVO> cartVOListTemp = new ArrayList<>();
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            if (cartVO.getSelected().equals(Constant.Cart.CHECKED)) {
                cartVOListTemp.add(cartVO);
            }
        }
        cartVOList = cartVOListTemp;
        //如果购物车已勾选的为空，报错
        if (CollectionUtils.isEmpty(cartVOList)) {
            throw new XidianMallException(XidianMallExceptionEnum.CART_EMPTY);
        }
        //判断商品是否存在、上下架状态、库存
        validSalStatusAndStock(cartVOList);
        //把购物车对象转为订单item
        List<OrderItem> orderItemList = cartVOListToOrderItemList(cartVOList);
        //扣库存
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            Product product = productFeignClient.detailForFeign(orderItem.getProductId());
            int stock = product.getStock() - orderItem.getQuantity();
            if (stock < 0) {
                throw new XidianMallException(XidianMallExceptionEnum.NOT_ENOUGH);
            }
            productFeignClient.updateStock(product.getId(), stock);
        }
        //把购物车种已勾选的商品删除
        cleanCart(cartVOList);
        //生成订单
        Order order = new Order();
        //生成订单号，有独立的规则
        String orderNo = OrderCodeFactory.getOrderCode(Long.valueOf(userId));
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalPrice(totalPrice(orderItemList));
        order.setReceiverName(createOrderRequest.getReceiverName());
        order.setReceiverAddress(createOrderRequest.getReceiverAddress());
        order.setReceiverMobile(createOrderRequest.getReceiverMobile());
        order.setOrderStatus(Constant.OrderStatusEnum.NOT_PAID.getCode());
        order.setPostage(0);
        order.setPaymentType(1);
        //插入到Order表
        orderMapper.insertSelective(order);
        //循环保存每个商品到order_item表
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            orderItem.setOrderNo(order.getOrderNo());
            orderItemMapper.insertSelective(orderItem);
        }
        //返回结果
        return orderNo;
    }

    private Integer totalPrice(List<OrderItem> orderItemList) {
        Integer totalPrice = 0;
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    private void cleanCart(List<CartVO> cartVOList) {
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            cartMapper.deleteByPrimaryKey(cartVO.getId());
        }
    }

    private List<OrderItem> cartVOListToOrderItemList(List<CartVO> cartVOList) {
        List<OrderItem> orderItemList = new ArrayList<>();
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartVO.getProductId());
            orderItem.setProductName(cartVO.getProductName());
            orderItem.setProductImg(cartVO.getProductImage());
            orderItem.setUnitPrice(cartVO.getPrice());
            orderItem.setQuantity(cartVO.getQuantity());
            orderItem.setTotalPrice(cartVO.getTotalPrice());
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }

    private void validSalStatusAndStock(List<CartVO> cartVOList) {
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            Product product = productFeignClient.detailForFeign(cartVO.getProductId());
            //判断商品是否存在，商品是否上架
            if (product == null || product.getStatus().equals(Constant.SaleStatus.NOT_SALE)) {
                throw new XidianMallException(XidianMallExceptionEnum.NOT_SALE);
            }
            //判断商品库存
            if (cartVO.getQuantity() > product.getStock()) {
                throw new XidianMallException(XidianMallExceptionEnum.NOT_ENOUGH);
            }
        }
    }

    @Override
    public OrderVO detail(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //订单不存在则报错
        if (order == null) {
            throw new XidianMallException(XidianMallExceptionEnum.NO_ORDER);
        }
        //订单存在，需要判断所属
        Integer userId = userFeignClient.getUser().getId();
        if (!order.getUserId().equals(userId)) {
            throw new XidianMallException(XidianMallExceptionEnum.NO_YOUR_ORDER);
        }
        OrderVO orderVO = getOrderVO(order);
        return orderVO;
    }

    private OrderVO getOrderVO(Order order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        //获取订单对应的orderItemVOList
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem, orderItemVO);
            orderItemVOList.add(orderItemVO);
        }
        orderVO.setOrderItemVOList(orderItemVOList);
        orderVO.setOrderStatusName(Constant.OrderStatusEnum.codeOf(orderVO.getOrderStatus()).getValue());
        return orderVO;
    }

    @Override
    public PageInfo listForCustomer(Integer pageNum, Integer pageSize) {
        Integer userId = userFeignClient.getUser().getId();
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectForCustomer(userId);
        List<OrderVO>  orderVOList = orderListToOrderVOList (orderList);
        PageInfo pageInfo = new PageInfo<>(orderList);
        pageInfo.setList(orderVOList);
        return pageInfo;
    }

    private List<OrderVO> orderListToOrderVOList(List<Order> orderList) {
        List<OrderVO> orderVOList = new ArrayList<>();
        for (int i = 0; i < orderList.size(); i++) {
            Order order = orderList.get(i);
            OrderVO orderVO = getOrderVO(order);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    @Override
    public void cancel(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查不到订单，报错
        if (order == null) {
            throw new XidianMallException(XidianMallExceptionEnum.NO_ORDER);
        }
        //验证用户身份
        Integer userId = userFeignClient.getUser().getId();
        if (!order.getUserId().equals(userId)) {
            throw new XidianMallException(XidianMallExceptionEnum.NO_YOUR_ORDER);
        }
        if (order.getOrderStatus().equals(Constant.OrderStatusEnum.NOT_PAID.getCode())) {
            order.setOrderStatus(Constant.OrderStatusEnum.CANCELED.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        } else {
            throw new XidianMallException(XidianMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }

    @Override
    public String qrcode(String orderNo) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String address = ip + ":" + port;
        String payUrl = "http://" + address + "/cart-order/pay?orderNo=" + orderNo;
        try {
            QRCodeGenerator.generateQRCodeImage(payUrl, 350, 350, FILE_UPLOAD_DIR + orderNo + ".png");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pngAddress = "http://" + address + "/cart-order/images/" + orderNo + ".png";
        return pngAddress;
    }

    @Override
    public void pay(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查不到订单，报错
        if (order == null) {
            throw new XidianMallException(XidianMallExceptionEnum.NO_ORDER);
        }
        if (order.getOrderStatus() == Constant.OrderStatusEnum.NOT_PAID.getCode()) {
            order.setOrderStatus(Constant.OrderStatusEnum.PAID.getCode());
            order.setPayTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        } else {
            throw new XidianMallException(XidianMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }

    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllForAdmin();
        List<OrderVO>  orderVOList = orderListToOrderVOList (orderList);
        PageInfo pageInfo = new PageInfo<>(orderList);
        pageInfo.setList(orderVOList);
        return pageInfo;
    }

    //发货
    @Override
    public void deliver(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查不到订单，报错
        if (order == null) {
            throw new XidianMallException(XidianMallExceptionEnum.NO_ORDER);
        }
        if (order.getOrderStatus() == Constant.OrderStatusEnum.PAID.getCode()) {
            order.setOrderStatus(Constant.OrderStatusEnum.DELIVERED.getCode());
            order.setDeliveryTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        } else {
            throw new XidianMallException(XidianMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }

    //订单完结
    @Override
    public void finish(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        //查不到订单，报错
        if (order == null) {
            throw new XidianMallException(XidianMallExceptionEnum.NO_ORDER);
        }
        //如果是普通用户，就要校验订单所属
        if (userFeignClient.getUser().getRole().equals(1) && !order.getUserId().equals(userFeignClient.getUser().getId())) {
            throw new XidianMallException(XidianMallExceptionEnum.NO_YOUR_ORDER);
        }
        //发货后可以完结订单
        if (order.getOrderStatus() == Constant.OrderStatusEnum.DELIVERED.getCode()) {
            order.setOrderStatus(Constant.OrderStatusEnum.FINISHED.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        } else {
            throw new XidianMallException(XidianMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }
}
