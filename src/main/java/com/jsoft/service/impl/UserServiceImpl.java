package com.jsoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jsoft.pojo.User;
import com.jsoft.service.UserService;
import com.jsoft.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
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
        final String salt = "F4EN";
        //拼接盐值和密码,再转换为字节数组并生成MD5摘要
        String encryptPassword = DigestUtils.md5DigestAsHex((salt + userAccount).getBytes(StandardCharsets.UTF_8));
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
}




