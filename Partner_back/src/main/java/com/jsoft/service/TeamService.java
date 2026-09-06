package com.jsoft.service;

import com.jsoft.pojo.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jsoft.pojo.entity.User;

/**
* @author Administrator
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2026-09-06 15:53:58
*/
public interface TeamService extends IService<Team> {
    /**
     * 新增队伍
     * @param team
     * @param loginUser
     * @return
     */
    Long addTeam(Team team, User loginUser);
}
