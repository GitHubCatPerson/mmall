package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {
    @Autowired
    IUserService iUserService;
    @Autowired
    ICategoryService iCategoryService;

    /**
     * 管理员添加品类  需要验证登录账号是否为管理员
     *
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", required = false, defaultValue = "0") int parentId) {
        //验证是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //是管理员---增加处理分类的逻辑
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 更新categoryName  需要判断是否为管理员  并根据categoryId更新
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_categoryName.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //是管理员---更新categoryName
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        } else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }

    }

    /**
     * 根据id获取当前id下子节点的信息 平级的  不递归
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "categoryId",required = false,defaultValue = "0")Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //是管理员---查询子节点的category信息，并且不递归，保持平级
            return iCategoryService.getChildParallerlCategory(categoryId);
        } else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 根据id获取当前id下子节点的信息 平级的  递归
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrendCategory(HttpSession session,@RequestParam(value = "categoryId",required = false,defaultValue = "0")Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //是管理员---查询当前节点的id和递归子节点的id
            return iCategoryService.selectCategoryAndChildrenById(categoryId);

        } else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }
}
