package com.jsoft.service;

import com.jsoft.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author F4EN
* @description 用户服务
* @createDate 2026-08-06 23:53:41
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    Long userRegister(String userAccount, String userPassword,String checkPassword);

}

