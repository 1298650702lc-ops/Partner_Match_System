package com.jsoft.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsoft.pojo.Dto.UserVo;
import com.jsoft.pojo.entity.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2026-09-06 15:56:16
*/
public interface UserTeamService extends IService<UserTeam> {
    /**
     * 根据队伍id获取用户列表
     * @param teamId
     * @param page
     * @return
     */
    List<UserVo> getUserListByTeamId(Long teamId, Page<UserVo> page);
}
