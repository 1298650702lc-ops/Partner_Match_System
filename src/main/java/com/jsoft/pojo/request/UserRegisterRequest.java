package com.jsoft.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求体
 * @Author F4EN
 * @createDate 2026-08-06 23:53:41
 */
@Data
public class UserRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7934531693832400877L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;

}
