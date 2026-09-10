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
     * id
     */
    private Long id;
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
    /**
     * 搜索词(同时对队伍名称和描述进行模糊搜索)
     */
    private String searchText;
}
