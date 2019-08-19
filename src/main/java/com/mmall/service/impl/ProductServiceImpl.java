package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    ICategoryService iCategoryService;


    /**
     * 新增产品 或 更新产品
     *
     * @param product
     * @return
     */
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            if (StringUtils.isNoneBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (product.getId() != null) {
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0) {
                    return ServerResponse.creatBySuccess("更新产品成功");
                }
                return ServerResponse.creatBySuccess("更新产品失败");
            } else {
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0) {
                    return ServerResponse.creatBySuccess("新增产品成功");
                }
                return ServerResponse.creatBySuccess("新增产品失败");
            }
        }
        return ServerResponse.creatByErrorMessage("新增或更新产品参数不正确");
    }

    /**
     * 修改产品的销售状态  上架  and  下架
     *
     * @param productId
     * @param status
     * @return
     */
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.creatBySuccess("修改产品销售状态成功");
        }
        return ServerResponse.creatByErrorMessage("修改产品销售状态失败");
    }


    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.creatByErrorMessage("产品已下架或者删除");
        }

        //vo对象 --value Object
        //pojo-->BO(business Object)-->VO(view Object)
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.creatBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setCaregoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        //imageHost
        Category category = categoryMapper.selectByPrimaryKey(product.getId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);//默认根节点
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        //createTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;


    }

    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        //1-startPage---start
        //2-填充自己的sql查询逻辑
        //3-pageHelper---收尾
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        //List转成ListVo
        List<ProductListVo> productListVolist = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVolist.add(productListVo);
        }
        //分页
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVolist);
        return ServerResponse.creatBySuccess(pageResult);


    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNoneBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        //List转成ListVo
        List<ProductListVo> productListVolist = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVolist.add(productListVo);
        }
        //分页
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVolist);
        return ServerResponse.creatBySuccess(pageResult);

    }

    /**
     * 用户端商品详情
     *
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.creatByErrorMessage("产品已下架或者删除");
        }
        if (product.getStatus() != Const.ProducttatusEnum.ON_SALE.getCode()) {
            return ServerResponse.creatByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.creatBySuccess(productDetailVo);
    }

    /**
     * 用户端商品搜索 及分页显示
     *
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @return
     */
//    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy) {
//        if (StringUtils.isBlank(keyword) && categoryId == null) {
//            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
//        }
//        List<Integer> categoryIdList = new ArrayList<Integer>();
//        if (categoryId != null) {
//            Category category = categoryMapper.selectByPrimaryKey(categoryId);
//            if (category == null && StringUtils.isBlank(keyword)) {
//                //没有该分类，并且还没有关键字，这个时候返回空的结果集，不报错
//                PageHelper.startPage(pageNum, pageSize);
//                List<ProductListVo> productListVoList = Lists.newArrayList();
//                PageInfo pageInfo = new PageInfo(productListVoList);
//                return ServerResponse.creatBySuccess(pageInfo);
//            }
//            //递归
//            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
//        }
//        if (StringUtils.isNotBlank(keyword)) {
//            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
//        }
//        PageHelper.startPage(pageNum, pageSize);
//        //排序处理
//        if (StringUtils.isNotBlank(orderBy)) {
//            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
//                String[] orderByArray = orderBy.split("_");
//                PageHelper.orderBy(orderByArray[0] + "" + orderByArray[1]);
//            }
//        }
//        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword, categoryIdList.size() == 0 ? null : categoryIdList);
//        List<ProductListVo> productListVoList = Lists.newArrayList();
//        for (Product product : productList) {
//            ProductListVo productListVo = assembleProductListVo(product);
//            productListVoList.add(productListVo);
//        }
//        //分页
//        PageInfo pageInfo = new PageInfo(productList);
//        pageInfo.setList(productListVoList);
//        return ServerResponse.creatBySuccess(pageInfo);
//    }
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();

        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                //没有该分类,并且还没有关键字,这个时候返回一个空的结果集,不报错
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.creatBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum, pageSize);
        //排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword, categoryIdList.size() == 0 ? null : categoryIdList);

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.creatBySuccess(pageInfo);
    }
}


