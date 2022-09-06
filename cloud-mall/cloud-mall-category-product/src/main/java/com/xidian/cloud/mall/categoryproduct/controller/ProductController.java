package com.xidian.cloud.mall.categoryproduct.controller;

import com.github.pagehelper.PageInfo;
import com.xidian.cloud.mall.categoryproduct.model.pojo.Product;
import com.xidian.cloud.mall.categoryproduct.model.request.ProductListRequest;
import com.xidian.cloud.mall.categoryproduct.service.ProductService;
import com.xidian.cloud.mall.common.ApiRestResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LDBX
 * 前台商品Controller
 */
@RestController
public class ProductController {

    @Autowired
    ProductService productService;

    @ApiOperation("商品详情")
    @GetMapping("product/detail")
    public ApiRestResponse detail(@RequestParam Integer id) {
        Product product = productService.detail(id);
        return ApiRestResponse.success(product);
    }

    @ApiOperation("商品详情")
    @GetMapping("product/list")
    public ApiRestResponse list(ProductListRequest productListRequest) {
        PageInfo list = productService.list(productListRequest);
        return ApiRestResponse.success(list);
    }

    @ApiOperation("商品详情")
    @GetMapping("product/detailForFeign")
    public Product detailForFeign(@RequestParam Integer id) {
        Product product = productService.detail(id);
        return product;
    }

    @PostMapping("product/updateStock")
    public void updateStock(@RequestParam Integer productId, @RequestParam Integer stock) {
        productService.updateStock(productId, stock);
    }
}
