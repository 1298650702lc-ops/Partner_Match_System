package com.jsoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jsoft.pojo.User;
import com.jsoft.service.UserService;
import com.jsoft.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author F4EN
 * @description 用户服务实现类
 * @createDate 2026-08-06 23:53:41
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Resource
    private UserMapper userMapper;
    /**
     * 盐值，用于密码加密
     */
    private static final String SALT = "F4EN";
    /**
     * 用户登录状态键
     */
    private static final String USER_LOGIN_STATE = "userLoginState";
    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 注册成功返回用户ID，失败返回-1
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验账号
        //账号密码长度规范
        if (userAccount == null || userPassword == null || checkPassword == null) {
            return -1L;
        }
        if(userAccount.length()<4) {
            return -1L;
        }
        if(userPassword.length()<8 || checkPassword.length()<8) {
            return -1L;
        }
        //账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        //先将字符串形式的正则表达式编译成Pattern对象,然后再用Pattern对象的matcher方法将字符串userAccount传入,得到一个Matcher对象
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        //查找是否有非法字符
        if(matcher.find()) {
            return -1L;
        }
        // 2.账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq("user_account",userAccount);
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            return -1L;
        }
        // 3.密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1L;
        }
        // 4.密码盐值加密
        //定义盐值
        //拼接盐值和密码,再转换为字节数组并生成MD5摘要
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userAccount).getBytes(StandardCharsets.UTF_8));
        // 4.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1L;
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 登录成功返回用户对象，失败返回null
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
     //1.校验账号
        //todo修改为自定义异常
        if(StringUtils.isEmpty(userAccount) || StringUtils.isEmpty(userPassword)) {
            return null;
        }
        if(userAccount.length()<4) {
            return null;
        }
        if(userPassword.length()<8 || StringUtils.isEmpty(userPassword)) {
            return null;
        }
        //账号不能包含特殊字符
        String validPattern= "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            return null;
        }
        //2.加密  登录密码加密校验
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userAccount).getBytes(StandardCharsets.UTF_8));
        //3.查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        queryWrapper.eq("user_password",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //4.用户不存在
        if(user == null) {
            return null;
        }
        //5.用户脱敏
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(userAccount);
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setCreateTime(user.getCreateTime());
        //6.记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        //6.返回登录用户的信息
        return user;
    }
}




