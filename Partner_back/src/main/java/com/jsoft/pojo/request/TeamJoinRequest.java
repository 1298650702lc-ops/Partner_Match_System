package com.jsoft.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
/**
 * 队伍加入请求体
 * @Author F4EN
 */
public class TeamJoinRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -8819956481278211351L;
    /**
     * 队伍ID
     */
    private Long teamId;
    /**
     * 密码
     */
    private String password;
}
