package com.jsoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsoft.Common.BaseResponse;
import com.jsoft.Common.ErrorCode;
import com.jsoft.Common.ResultUtil;
import com.jsoft.exception.BusinessException;
import com.jsoft.pojo.Dto.TeamQuery;
import com.jsoft.pojo.Dto.TeamUserVo;
import com.jsoft.pojo.Dto.UserVo;
import com.jsoft.pojo.entity.Team;
import com.jsoft.pojo.entity.User;
import com.jsoft.pojo.request.*;
import com.jsoft.service.TeamService;
import com.jsoft.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 队伍接口
 *
 * @Author F4EN
 *
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;

    /**
     * 新增队伍
     *
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User LoginUser = userService.getCurrentUser(request);
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamAddRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍信息转换失败");
        }
        Long teamId = teamService.addTeam(team, LoginUser);
        return ResultUtil.success(teamId);
    }

    /**
     * 删除队伍
     *
     * @param id
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestParam Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        Boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        return ResultUtil.success(true);
    }

    /**
     * 更新队伍
     *
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    @PutMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新队伍信息为空");
        }
        User loginUser = userService.getCurrentUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
        }
        return ResultUtil.success(true);
    }

    /**
     * 根据id查询队伍
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询队伍id错误");
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "查询队伍为空");
        }
        return ResultUtil.success(team);
    }

    /**
     * 查询队伍列表
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> getTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询队伍参数为空");
        }
        Boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVo> teamList = teamService.getTeamList(teamQuery, isAdmin);
        return ResultUtil.success(teamList);
    }

    /**
     * 分页查询队伍列表
     *
     * @param teamQuery
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> getTeamListByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询队伍参数为空");
        }
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询队伍参数转换失败");
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtil.success(teamPage);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null || teamJoinRequest.getTeamId() == null || teamJoinRequest.getTeamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入队伍参数为空");
        }
        User loginUser = userService.getCurrentUser(request);
        Boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtil.success(result);
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null || teamQuitRequest.getTeamId() == null || teamQuitRequest.getTeamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "退出队伍参数为空");
        }
        User loginUser = userService.getCurrentUser(request);
        // 退出队伍逻辑
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtil.success(result);
    }

    /**
     * 获取我加入的队伍列表
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> getMyJoinTeamList(HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        List<TeamUserVo> teamList = teamService.getMyJoinTeamList(loginUser);
        return ResultUtil.success(teamList);
    }

    /**
     * 获取队伍下的用户列表
     * @param teamSearchUserRequest
     * @param request
     * @return
     */
    @GetMapping("/list/user")
    public BaseResponse<Page<UserVo>> getUserListInTeam(TeamSearchUserRequest teamSearchUserRequest, HttpServletRequest request) {
        if(teamSearchUserRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询队伍下的用户参数为空");
        }
        if(teamSearchUserRequest.getTeamId() == null || teamSearchUserRequest.getTeamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询队伍id错误");
        }
        User loginUser = userService.getCurrentUser(request);
        Page<UserVo> userList = teamService.getUserListInTeam(teamSearchUserRequest,loginUser);
        return ResultUtil.success(userList);
    }
}
