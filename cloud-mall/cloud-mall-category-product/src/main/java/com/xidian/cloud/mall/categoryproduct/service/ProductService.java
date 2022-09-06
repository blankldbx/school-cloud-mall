package com.xidian.cloud.mall.categoryproduct.service;

import com.github.pagehelper.PageInfo;
import com.xidian.cloud.mall.categoryproduct.model.pojo.Product;
import com.xidian.cloud.mall.categoryproduct.model.request.AddProductRequest;
import com.xidian.cloud.mall.categoryproduct.model.request.ProductListRequest;

/**
 * @author LDBX
 * 商品Service
 */
public interface ProductService {

    void add(AddProductRequest addProductRequest);

    void update(Product updateProduct);

    void delete(Integer id);

    void batchUpdateSellStatus(Integer[] ids, Integer sellStatus);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    Product detail(Integer id);

    PageInfo list(ProductListRequest productListRequest);

    void updateStock(Integer productId, Integer stock);
}
