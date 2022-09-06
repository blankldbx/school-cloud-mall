package com.xidian.cloud.mall.categoryproduct.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xidian.cloud.mall.categoryproduct.model.dao.ProductMapper;
import com.xidian.cloud.mall.categoryproduct.model.pojo.Product;
import com.xidian.cloud.mall.categoryproduct.model.query.ProductListQuery;
import com.xidian.cloud.mall.categoryproduct.model.request.AddProductRequest;
import com.xidian.cloud.mall.categoryproduct.model.request.ProductListRequest;
import com.xidian.cloud.mall.categoryproduct.model.vo.CategoryVO;
import com.xidian.cloud.mall.categoryproduct.service.CategoryService;
import com.xidian.cloud.mall.categoryproduct.service.ProductService;
import com.xidian.cloud.mall.common.Constant;
import com.xidian.cloud.mall.exception.XidianMallException;
import com.xidian.cloud.mall.exception.XidianMallExceptionEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LDBX
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CategoryService categoryService;

    @Override
    public void add(AddProductRequest addProductRequest) {
        Product product = new Product();
        BeanUtils.copyProperties(addProductRequest, product);
        Product productOld = productMapper.selectByName(addProductRequest.getName());
        if (productOld == null) {
            throw new XidianMallException(XidianMallExceptionEnum.NAME_EXISTED);
        }
        int count = productMapper.insertSelective(product);
        if (count == 0) {
            throw new XidianMallException(XidianMallExceptionEnum.CREATE_FAILED);
        }
    }

    @Override
    public void update(Product updateProduct) {
        Product productOld = productMapper.selectByName(updateProduct.getName());
        //同名且不同id，不能继续修改
        if (productOld != null && !productOld.getId().equals(updateProduct.getId())) {
            throw new XidianMallException(XidianMallExceptionEnum.NAME_EXISTED);
        }
        int count = productMapper.updateByPrimaryKeySelective(updateProduct);
        if (count == 0) {
            throw new XidianMallException(XidianMallExceptionEnum.UPDATE_FAILED);
        }
    }

    @Override
    public void delete(Integer id) {
        Product productOld = productMapper.selectByPrimaryKey(id);
        //查不到该记录，无法删除
        if (productOld == null) {
            throw new XidianMallException(XidianMallExceptionEnum.DELETE_FAILED);
        }
        int count = productMapper.deleteByPrimaryKey(id);
        if (count == 0) {
            throw new XidianMallException(XidianMallExceptionEnum.DELETE_FAILED);
        }
    }

    @Override
    public void batchUpdateSellStatus(Integer[] ids, Integer sellStatus) {
        productMapper.batchUpdateSellStatus(ids, sellStatus);
    }

    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> products = productMapper.selectListForAdmin();
        PageInfo pageInfo = new PageInfo<>(products);
        return pageInfo;
    }

    @Override
    public Product detail(Integer id) {
        Product product = productMapper.selectByPrimaryKey(id);
        return product;
    }

    @Override
    public PageInfo list(ProductListRequest productListRequest) {
        //构建Query对象
        ProductListQuery productListQuery = new ProductListQuery();
        //搜索处理
        if (!StringUtils.isEmpty(productListRequest.getKeyword())) {
            String keyword = new StringBuilder().append("%").append(productListRequest.getKeyword()).append("%").toString();
            productListQuery.setKeyword(keyword);
        }

        //目录处理：如果查某个目录下的商品，不仅是需要查出该目录下的，还有把所有子目录的所有商品都查出来，所以要拿到一个目录id的list
        if (productListRequest.getCategoryId() != null) {
            List<CategoryVO> categoryVOList = categoryService.listCategoryForCustomer(productListRequest.getCategoryId());
            ArrayList<Integer> categoryIds = new ArrayList<>();
            categoryIds.add(productListRequest.getCategoryId());
            getCategoryIds(categoryVOList, categoryIds);
            productListQuery.setCategoryIds(categoryIds);
        }

        //排序处理
        String orderBy = productListRequest.getOrderBy();
        if (Constant.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
            PageHelper.startPage(productListRequest.getPageNum(), productListRequest.getPageSize(), orderBy);
        } else {
            PageHelper.startPage(productListRequest.getPageNum(), productListRequest.getPageSize());
        }

        List<Product> productList = productMapper.selectList(productListQuery);
        PageInfo pageInfo = new PageInfo(productList);
        return pageInfo;
    }

    private void getCategoryIds(List<CategoryVO> categoryVOList, ArrayList<Integer> categoryIds) {
        for (int i = 0; i < categoryVOList.size(); i++) {
            CategoryVO categoryVO = categoryVOList.get(i);
            if (categoryVO != null) {
                categoryIds.add(categoryVO.getId());
                getCategoryIds(categoryVO.getChildCategory(), categoryIds);
            }
        }
    }

    @Override
    public void updateStock(Integer productId, Integer stock) {
        Product product = new Product();
        product.setId(productId);
        product.setStock(stock);
        productMapper.updateByPrimaryKeySelective(product);
    }
}
