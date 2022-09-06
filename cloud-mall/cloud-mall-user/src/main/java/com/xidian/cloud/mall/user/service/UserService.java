package com.xidian.cloud.mall.user.service;


import com.xidian.cloud.mall.exception.XidianMallException;
import com.xidian.cloud.mall.user.model.pojo.User;

/**
 * @author LDBX
 * UserService
 */
public interface UserService {

    void register(String userName, String password) throws XidianMallException;

    User login(String userName, String password) throws XidianMallException;

    void updateInformation(User user) throws XidianMallException;

    boolean checkAdminRole(User user);
}
