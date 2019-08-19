package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    CategoryMapper categoryMapper;
    public ServerResponse addCategory(String categoryName,Integer parentId){
        if(parentId ==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.creatByErrorMessage("添加参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        int rowCount = categoryMapper.insert(category);
        if(rowCount>0){
            return  ServerResponse.creatBySuccess("添加品类成功");
        }
        return ServerResponse.creatByErrorMessage("添加品类失败");
    }

    /**
     * 更新categoryName
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse updateCategoryName(Integer categoryId,String categoryName){
        if(categoryId ==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.creatByErrorMessage("更新参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount>0){
            return  ServerResponse.creatBySuccess("更新品类名字成功");
        }
        return ServerResponse.creatByErrorMessage("更新品类名字失败");
    }

    /**
     * 查询节点 平级
     * @param categoryId
     * @return
     */
    public  ServerResponse<List<Category>> getChildParallerlCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCateChildByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return  ServerResponse.creatBySuccess(categoryList);
    }


    public ServerResponse<List<Integer> > selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if(categoryId!=null){
            for( Category categoryItem:categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.creatBySuccess(categoryIdList);

    }
//递归算法 自己调自己  算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);

        if(category!=null){
            categorySet.add(category);
        }
        //查询子节点，递归算法一定要有一个退出条件
        //mybatis 返回的集合即便是空 也不会返回null 不用做判断
        List<Category> categoryList = categoryMapper.selectCateChildByParentId(categoryId);

        for(Category categoryItem:categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;

    }
}
