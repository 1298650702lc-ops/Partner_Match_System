package com.jsoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jsoft.Common.BaseResponse;
import com.jsoft.Common.ErrorCode;
import com.jsoft.exception.BusinessException;
import com.jsoft.pojo.User;
import com.jsoft.service.UserService;
import com.jsoft.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jsoft.Constant.UserConstant.USER_LOGIN_STATE;

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
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 注册成功返回用户ID，失败返回-1
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1.校验账号
        //账号密码长度规范
        if (userAccount == null || userPassword == null || checkPassword == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"参数为空");
        }
        if(userAccount.length()<4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        if(userPassword.length()<8 || checkPassword.length()<8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        if(StringUtils.isEmpty(planetCode) || planetCode.length()>5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号不符合要求");
        }
        //账户不包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        //先将字符串形式的正则表达式编译成Pattern对象,然后再用Pattern对象的matcher方法将字符串userAccount传入,得到一个Matcher对象
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        //查找是否有非法字符
        if(matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        // 2.账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq("user_account",userAccount);
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }
        // 星球编号不能重复
        QueryWrapper<User> planetCodeQueryWrapper = new QueryWrapper<>();
        planetCodeQueryWrapper.eq("planet_code", planetCode);
        User existingPlanetCodeUser = userMapper.selectOne(planetCodeQueryWrapper);
        if (existingPlanetCodeUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号已存在");
        }
        // 3.密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和校验密码不一致");
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
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败");
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
            throw new BusinessException(ErrorCode.NULL_ERROR,"账号或密码为空");
        }
        if(userAccount.length()<4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        if(userPassword.length()<8 || StringUtils.isEmpty(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        //账号不能包含特殊字符
        String validPattern= "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        //5.用户脱敏
        User safetyUser = getSafeUser(user);
        //6.记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        //6.返回登录用户的信息
        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param orginUser 原始用户信息
     * @return safetyUser 脱敏后的用户信息
     */
    @Override
    public User getSafeUser(User orginUser) {
        User safetyUser = new User();
        safetyUser.setId(orginUser.getId());
        safetyUser.setUsername(orginUser.getUsername());
        safetyUser.setUserAccount(orginUser.getUserAccount());
        safetyUser.setAvatarUrl(orginUser.getAvatarUrl());
        safetyUser.setGender(orginUser.getGender());
        safetyUser.setEmail(orginUser.getEmail());
        safetyUser.setUserRole(orginUser.getUserRole());
        safetyUser.setPhone(orginUser.getPhone());
        safetyUser.setUserStatus(orginUser.getUserStatus());
        safetyUser.setCreateTime(orginUser.getCreateTime());
        safetyUser.setPlanetCode(orginUser.getPlanetCode());
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除用户登录状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




