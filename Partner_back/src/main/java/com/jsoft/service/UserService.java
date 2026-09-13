package com.jsoft.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsoft.pojo.Dto.UserVo;
import com.jsoft.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    Long userRegister(String userAccount, String userPassword,String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request HTTP请求对象
     * @return 用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param orginUser
     * @return
     */
    User getSafeUser(User orginUser);

    /**
     * 用户注销
     * @param request
     * @return 注销正常返回1
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户（缓存方式）
     *
     * @param tagList
     * @return
     */
    Page<User> SearchUserByTags(List<String> tagList, Long userId, Long pageSize, Long pageNum);

    /**
     * 根据标签搜索用户（SQL方式）
     * @param tagList
     * @return
     */
    List<User> SearchUserByTagsBySQL(List<String> tagList);

    /**
     * 获取当前登录用户
     * @param request
     * @return 当前登录用户
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 更新用户信息
     * @param user
     * @param loginuser
     * @return 更新成功返回1，失败返回0
     */
    int updateUser(User user, User loginuser);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取匹配的用户
     * @param num
     * @param loginuser
     * @return
     */
    List<User> matchUsers(long num, User loginuser);
}

