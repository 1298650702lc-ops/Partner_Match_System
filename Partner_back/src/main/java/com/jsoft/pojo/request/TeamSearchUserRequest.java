package com.jsoft.pojo.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamSearchUserRequest extends PageResult implements Serializable  {
    @Serial
    private static final long serialVersionUID = 6392918392968881303L;
    /**
     * 队伍ID
     */
    private Long teamId;
    /**
     * 队伍密码
     */
    private String password;
}
