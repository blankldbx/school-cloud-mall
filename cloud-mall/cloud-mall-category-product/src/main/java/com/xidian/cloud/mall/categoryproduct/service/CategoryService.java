package com.xidian.cloud.mall.categoryproduct.service;

import com.github.pagehelper.PageInfo;
import com.xidian.cloud.mall.categoryproduct.model.pojo.Category;
import com.xidian.cloud.mall.categoryproduct.model.request.AddCategoryRequest;
import com.xidian.cloud.mall.categoryproduct.model.vo.CategoryVO;

import java.util.List;

/**
 * @author LDBX
 */
public interface CategoryService {
    void add(AddCategoryRequest addCategoryRequest);

    void update(Category updateCategory);

    void delete(Integer id);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    List<CategoryVO> listCategoryForCustomer(Integer parentId);

}
