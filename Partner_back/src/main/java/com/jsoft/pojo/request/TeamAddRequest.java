package com.jsoft.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 队伍创建请求体
 * @Author F4EN
 */
@Data
public class TeamAddRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5954423043896942328L;
    /**
     * 队伍名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 最大人数
     */
    private Integer maxNum;
    /**
     * 过期时间
     */
    private Date expireTime;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 状态 0 - 公开 1 - 私有 2 - 加密
     */
    private Integer status;
    /**
     * 密码
     */
    private String password;
}
