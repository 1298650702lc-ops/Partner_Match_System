package com.jsoft.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 3469567662874274322L;
    /**
     * 当前页码
     */
    private Integer pageNum;
    /**
     * 每页条数
     */
    private Integer pageSize;
}
