package com.jsoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsoft.Common.BaseResponse;
import com.jsoft.Common.ErrorCode;
import com.jsoft.Common.ResultUtil;
import com.jsoft.exception.BusinessException;
import com.jsoft.pojo.entity.User;
import com.jsoft.pojo.request.UserLoginRequest;
import com.jsoft.pojo.request.UserRegisterRequest;
import com.jsoft.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jsoft.Constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @Author F4EN
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册信息不正确");
        }
        return ResultUtil.success(userService.userRegister(userAccount, userPassword, checkPassword, planetCode));
    }

    /**
     * 用户登录接口
     *
     * @param userLoginRequest
     * @param request
     * @return 登录用户信息
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "登录信息为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册信息不正确");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        return ResultUtil.success(user);

    }

    /**
     * 根据用户名称搜索用户
     *
     * @param username 用户名称
     * @return 搜索结果
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> userSearch(@RequestParam String username, HttpServletRequest request) {
        if (StringUtils.isEmpty(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名为空");
        }
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> users = userService.list(queryWrapper);
        return ResultUtil.success(users.stream().map(user -> userService.getSafeUser(user)).collect(Collectors.toList()));
    }

    /**
     * 根据id删除用户
     *
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> userDelete(@RequestBody long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不正确");
        }
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }
        return ResultUtil.success(userService.removeById(id));
    }

    /**
     * 检查是否为管理员
     */
    public Boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == 1;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return 当前登录用户
     */
    @GetMapping("/current")
    public BaseResponse<User> getUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafeUser(user);
        return ResultUtil.success(safetyUser);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return 注销成功返回1，失败返回0
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注销用户不存在");
        }
        return ResultUtil.success(userService.userLogout(request));
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return 搜索结果
     */
    @GetMapping("/search/tags")
    public BaseResponse<Page<User>> searchUserByTags(
            @RequestParam List<String> tagNameList,
            @RequestParam(defaultValue = "8") long pageSize,
            @RequestParam(defaultValue = "1") long pageNum,
            HttpServletRequest request) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签列表为空");
        }
        if (pageSize <= 0 || pageNum <= 0 || pageSize > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数不正确");
        }
        User userCurrent = userService.getCurrentUser(request);
        Page<User> users = userService.SearchUserByTags(tagNameList, userCurrent.getId(), pageSize, pageNum);
        return ResultUtil.success(users);
    }

    /**
     * 更新用户信息
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> UpdateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginuser = userService.getCurrentUser(request);
        int result = userService.updateUser(user, loginuser);
        return ResultUtil.success(result);
    }

    /**
     * 根据登录用户信息推荐首页用户
     *
     * @param request
     * @return 搜索结果
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> userSearch(
            @RequestParam(defaultValue = "8") long pageSize,
            @RequestParam(defaultValue = "1") long pageNum,
            HttpServletRequest request) {
        if (pageSize <= 0 || pageNum <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分页参数必须大于0");
        }
        User loginuser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginuser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        // 分页参数必须参与缓存 Key，否则第 1 页缓存会被后续页重复使用。
        String redisKey = String.format(
                "user:recommend:%s:%d:%d",
                loginuser.getId(),
                pageNum,
                pageSize
        );
        Page<User> userList = (Page<User>) redisTemplate.opsForValue().get(redisKey);
        //如果没有缓存，执行查询并创建缓存
        if (userList == null) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            userList = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
            // 先脱敏再写入缓存，避免 Redis 中保存用户密码等敏感字段。
            if (userList.getRecords() != null) {
                List<User> safeUserList = userList.getRecords().stream()
                        .map(user -> userService.getSafeUser(user))
                        .collect(Collectors.toList());
                userList.setRecords(safeUserList);
            }
            try {
                redisTemplate.opsForValue().set(redisKey, userList, 30, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("redis set Key error", e);
            }
        }
        return ResultUtil.success(userList);
    }
}
