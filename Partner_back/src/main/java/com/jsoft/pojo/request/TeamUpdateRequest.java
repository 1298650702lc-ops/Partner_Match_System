package com.jsoft.pojo.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class TeamUpdateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -6484059720480877532L;
    private Long id;
    private String name;
    private String description;
    private Integer status;
    private String password;
    private Date expireTime;
}
