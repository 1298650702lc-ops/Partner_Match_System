package com.jsoft.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsoft.pojo.Dto.TeamQuery;
import com.jsoft.pojo.Dto.TeamUserVo;
import com.jsoft.pojo.Dto.UserVo;
import com.jsoft.pojo.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jsoft.pojo.entity.User;
import com.jsoft.pojo.request.TeamJoinRequest;
import com.jsoft.pojo.request.TeamQuitRequest;
import com.jsoft.pojo.request.TeamSearchUserRequest;
import com.jsoft.pojo.request.TeamUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    /**
     * 获取队伍列表
     * @param teamQuery
     * @param loginUser
     * @return
     */
    List<TeamUserVo> getTeamList(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);
    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param id
     * @param loginUser
     * @return
     */
    Boolean deleteTeam(Long id, User loginUser);

    /**
     * 获取我加入的队伍列表
     * @param loginUser
     * @return
     */
    List<TeamUserVo> getMyJoinTeamList(User loginUser);

    /**
     * 根据队伍id获取队伍成员列表
     * @param teamId
     * @param loginUser
     * @return
     */
    Page<UserVo> getUserListInTeam(TeamSearchUserRequest teamSearchUserRequest, User loginUser);
}
