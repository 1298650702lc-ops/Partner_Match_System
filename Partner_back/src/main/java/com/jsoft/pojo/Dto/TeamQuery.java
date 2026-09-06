package com.jsoft.pojo.Dto;

import com.jsoft.pojo.request.PageResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询封装类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQuery extends PageResult {
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
     * 用户ID
     */
    private Long userId;
    /**
     * 状态 0 - 公开 1 - 加密 2 - 私有
     */
    private Integer status;
}
