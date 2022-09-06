package com.xidian.cloud.mall.user.controller;


import com.xidian.cloud.mall.common.ApiRestResponse;
import com.xidian.cloud.mall.common.Constant;
import com.xidian.cloud.mall.exception.XidianMallException;
import com.xidian.cloud.mall.exception.XidianMallExceptionEnum;
import com.xidian.cloud.mall.user.model.pojo.User;
import com.xidian.cloud.mall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * 用户控制器
 */
@Controller
public class UserController {

    @Autowired
    UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ResponseBody
    public ApiRestResponse register(@RequestParam("userName") String userName, @RequestParam("password") String password) throws XidianMallException {
        if (StringUtils.isEmpty(userName)) {
            return ApiRestResponse.error(XidianMallExceptionEnum.NEED_USER_NAME);
        }
        if (StringUtils.isEmpty(password)) {
            return ApiRestResponse.error(XidianMallExceptionEnum.NEED_PASSWORD);
        }
        //密码长度不能少于8位
        if (password.length() < 8) {
            return ApiRestResponse.error(XidianMallExceptionEnum.PASSWORD_TOO_SHORT);
        }
        userService.register(userName, password);
        return ApiRestResponse.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ResponseBody
    public ApiRestResponse login(@RequestParam("userName") String userName, @RequestParam("password") String password, HttpSession session) throws XidianMallException {
        if (StringUtils.isEmpty(userName)) {
            return ApiRestResponse.error(XidianMallExceptionEnum.NEED_USER_NAME);
        }
        if (StringUtils.isEmpty(password)) {
            return ApiRestResponse.error(XidianMallExceptionEnum.NEED_PASSWORD);
        }
        User user = userService.login(userName, password);
        //保存用户信息时，不保存密码
        user.setPassword(null);
        session.setAttribute(Constant.IMOOC_MALL_USER, user);
        return ApiRestResponse.success(user);
    }

    /**
     * 更新个性签名
     */
    @PostMapping("/user/update")
    @ResponseBody
    public ApiRestResponse updateUserInfo(HttpSession session, @RequestParam String signature) throws XidianMallException {
        User currentUser = (User) session.getAttribute(Constant.IMOOC_MALL_USER);
        if (currentUser == null) {
            return ApiRestResponse.error(XidianMallExceptionEnum.NEED_LOGIN);
        }
        User user = new User();
        user.setId(currentUser.getId());
        user.setPersonalizedSignature(signature);
        userService.updateInformation(user);
        return ApiRestResponse.success();
    }

    /**
     * 登出，清除session
     *
     * @param session
     * @return
     */
    @PostMapping("/user/logout")
    @ResponseBody
    public ApiRestResponse logout(HttpSession session) {
        session.removeAttribute(Constant.IMOOC_MALL_USER);
        return ApiRestResponse.success();
    }

    /**
     * 管理员登录接口
     *
     * @param userName
     * @param password
     * @param session
     * @return
     * @throws XidianMallException
     */
    @PostMapping("/adminLogin")
    @ResponseBody
    public ApiRestResponse adminLogin(@RequestParam("userName") String userName, @RequestParam("password") String password, HttpSession session) throws XidianMallException {
        if (StringUtils.isEmpty(userName)) {
            return ApiRestResponse.error(XidianMallExceptionEnum.NEED_USER_NAME);
        }
        if (StringUtils.isEmpty(password)) {
            return ApiRestResponse.error(XidianMallExceptionEnum.NEED_PASSWORD);
        }
        User user = userService.login(userName, password);
        //校验是否是管理员
        if (userService.checkAdminRole(user)) {
            //是管理员，执行操作
            //保存用户信息时，不保存密码
            user.setPassword(null);
            session.setAttribute(Constant.IMOOC_MALL_USER, user);
            return ApiRestResponse.success(user);
        } else {
            return ApiRestResponse.error(XidianMallExceptionEnum.NEED_ADMIN);
        }
    }

    /**
     * 检验是否是管理员
     * @param user
     * @return
     */
    @PostMapping("/checkAdminRole")
    @ResponseBody
    public boolean checkAdminRole(@RequestBody User user) {
        return userService.checkAdminRole(user);
    }

    /**
     * 获取当前登录的User对象
     * @param session
     * @return
     */
    @GetMapping("/getUser")
    @ResponseBody
    public User getUser(HttpSession session) {
        User currentUser = (User) session.getAttribute(Constant.IMOOC_MALL_USER);
        return currentUser;
    }
}
