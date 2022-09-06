package com.xidian.cloud.mall.common;

import com.google.common.collect.Sets;
import com.xidian.cloud.mall.exception.XidianMallException;
import com.xidian.cloud.mall.exception.XidianMallExceptionEnum;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author LDBX
 * 常量值
 */
@Component
public class Constant {
    public static final String IMOOC_MALL_USER = "imooc_mall_user";
    public static final String SALT = "8svbsvjkweDF,.03[";

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price desc", "price asc");
    }

    public interface SaleStatus {
        int NOT_SALE = 0;//商品下架状态
        int SALE = 1;//商品上架状态
    }

    public interface Cart {
        int UN_CHECKED = 0;//商品未选中状态
        int CHECKED = 1;//商品选中状态
    }

    public enum OrderStatusEnum {
        CANCELED(0, "用户已取消"),
        NOT_PAID(10, "未付款"),
        PAID(20, "已付款"),
        DELIVERED(30, "已发货"),
        FINISHED(40, "交易完成");

        private int code;
        private String value;

        OrderStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public static OrderStatusEnum codeOf(int code) {
            for (OrderStatusEnum orderStatusEnum : values()) {
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new XidianMallException(XidianMallExceptionEnum.NO_ENUM);
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
