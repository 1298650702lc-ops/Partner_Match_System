package com.jsoft.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    /**
     * 用户登录请求体
     * @Author F4EN
     */
    @Serial
    private static final long serialVersionUID = 7934531693832400877L;
    private String userAccount;
    private String userPassword;
}
