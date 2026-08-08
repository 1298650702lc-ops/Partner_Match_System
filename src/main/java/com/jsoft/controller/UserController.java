package com.jsoft.controller;

import com.jsoft.pojo.Result;
import com.jsoft.pojo.User;
import com.jsoft.pojo.request.UserLoginRequest;
import com.jsoft.pojo.request.UserRegisterRequest;
import com.jsoft.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户接口
 *
 * @Author F4EN
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册接口
     *
     * @return 用户注册成功返回新用户id，失败返回-1
     */
    @PutMapping("/register")
    public Result userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        return Result.success(userService.userRegister(userAccount, userPassword, checkPassword));
    }

    /**
     * 用户登录接口
     * @param userLoginRequest
     * @param request
     * @return 登录用户信息
     */
    @PostMapping("/login")
    public Result userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        if (user == null) {
            return Result.error("登录失败");
        }
        return Result.success(user);

    }
}
