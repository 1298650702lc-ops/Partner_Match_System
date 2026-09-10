package com.jsoft.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 队伍退出请求体
 * @Author F4EN
 */
@Data
public class TeamQuitRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -4405650953462344374L;
    /**
     * 队伍ID
     */
    private Long teamId;
    /**
     * 用户ID
     */
    private Long userId;
}
