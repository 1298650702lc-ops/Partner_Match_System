package com.jsoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jsoft.Common.BaseResponse;
import com.jsoft.Common.ErrorCode;
import com.jsoft.Common.ResultUtil;
import com.jsoft.exception.BusinessException;
import com.jsoft.pojo.User;
import com.jsoft.pojo.request.UserLoginRequest;
import com.jsoft.pojo.request.UserRegisterRequest;
import com.jsoft.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.jsoft.Constant.UserConstant.USER_LOGIN_STATE;

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
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        //校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册信息为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"注册信息不正确");
        }
        return ResultUtil.success(userService.userRegister(userAccount, userPassword, checkPassword, planetCode));
    }

    /**
     * 用户登录接口
     * @param userLoginRequest
     * @param request
     * @return 登录用户信息
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"登录信息为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"注册信息不正确");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        return ResultUtil.success(user);

    }

    /**
     * 根据用户名称搜索用户
     * @param username 用户名称
     * @return 搜索结果
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> userSearch(@RequestParam String username,HttpServletRequest request) {
        if (StringUtils.isEmpty(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名为空");
        }
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"用户无权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> users = userService.list(queryWrapper);
        return ResultUtil.success(users.stream().map(user -> userService.getSafeUser(user)).collect(Collectors.toList()));
    }

    /**
     * 根据id删除用户
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> userDelete(@RequestBody long id ,HttpServletRequest request) {
        if(id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户ID不正确");
        }
        if(!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH,"用户无权限");
        }
        return ResultUtil.success(userService.removeById(id));
    }
    /**
     * 检查是否为管理员
     */
    public Boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;
        return user != null && user.getUserRole() == 1;
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User)userObj;
        if(currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"用户未登录");
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafeUser(user);
        return ResultUtil.success(safetyUser);
    }
    /**
     * 用户注销
     * @param request
     * @return 注销成功返回1，失败返回0
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"注销用户不存在");
        }
        return ResultUtil.success(userService.userLogout(request));
    }
}
