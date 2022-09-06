package com.xidian.cloud.mall.user.service.impl;


import com.xidian.cloud.mall.exception.XidianMallException;
import com.xidian.cloud.mall.exception.XidianMallExceptionEnum;
import com.xidian.cloud.mall.user.model.dao.UserMapper;
import com.xidian.cloud.mall.user.model.pojo.User;
import com.xidian.cloud.mall.user.service.UserService;
import com.xidian.cloud.mall.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;


/**
 * @author LDBX
 * UserService实现类
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public void register(String userName, String password) throws XidianMallException {
        //查询用户名是否存在，不允许重名
        User result = userMapper.selectByName(userName);
        if (result != null) {
            throw new XidianMallException(XidianMallExceptionEnum.NAME_EXISTED);
        }

        //写到数据库
        User user = new User();
        user.setUsername(userName);
        user.setPassword(MD5Utils.md5(password));
        int count = userMapper.insertSelective(user);
        if (count == 0) {
            throw new XidianMallException(XidianMallExceptionEnum.INSERT_FAILED);
        }
    }

    @Override
    public User login(String userName, String password) throws XidianMallException {
        String md5Psaaword = null;
        md5Psaaword = MD5Utils.md5(password);
        User user = userMapper.selectLogin(userName, md5Psaaword);
        if (user == null) {
            throw new XidianMallException(XidianMallExceptionEnum.WRONG_PASSWORD);
        }
        return user;
    }

    @Override
    public void updateInformation(User user) throws XidianMallException {
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 1) {
            throw new XidianMallException(XidianMallExceptionEnum.UPDATE_FAILED);
        }
    }

    @Override
    public boolean checkAdminRole(User user) {
        //1是普通用户，2是管理员
        return user.getRole().equals(2);
    }

}
