package com.jsoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jsoft.Common.ErrorCode;
import com.jsoft.Common.TeamStatusEnum;
import com.jsoft.exception.BusinessException;
import com.jsoft.pojo.Dto.TeamQuery;
import com.jsoft.pojo.Dto.TeamUserVo;
import com.jsoft.pojo.Dto.UserVo;
import com.jsoft.pojo.entity.Team;
import com.jsoft.pojo.entity.User;
import com.jsoft.pojo.entity.UserTeam;
import com.jsoft.pojo.request.TeamJoinRequest;
import com.jsoft.pojo.request.TeamQuitRequest;
import com.jsoft.pojo.request.TeamSearchUserRequest;
import com.jsoft.pojo.request.TeamUpdateRequest;
import com.jsoft.service.TeamService;
import com.jsoft.mapper.TeamMapper;
import com.jsoft.service.UserService;
import com.jsoft.service.UserTeamService;
import lombok.NonNull;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.impl.internal.resilience.RobustResilienceStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2026-09-06 15:53:58
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;

    /**
     * 新增队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTeam(Team team, User loginUser) {
        //1.是否请求参数为空
        if (team == null || loginUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍信息或用户信息为空");
        }
        //2.是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        //3.校验信息是否合法
        //队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum <= 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不合法");
        }
        //队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不合法");
        }
        // 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isBlank(description) || description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }

        //status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumById(status);
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (teamStatusEnum == TeamStatusEnum.SECRET) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不合法");
            }
        }
        //超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍超时时间不合法");
        }
        //校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        long count = this.count(queryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        boolean result = this.save(team);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(team.getUserId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        boolean result1 = userTeamService.save(userTeam);
        if (!result1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        return team.getId();
    }

    /**
     * 查询队伍列表
     *
     * @param teamQuery
     * @return
     */
    @Override
    public List<TeamUserVo> getTeamList(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null) {
                queryWrapper.eq("max_num", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("user_id", userId);
            }
            //根据状态查询
            Integer status = teamQuery.getStatus();
            if (status == null) {
                // 默认展示公开和加密队伍
                queryWrapper.in("status",
                        TeamStatusEnum.PUBLIC.getValue(),
                        TeamStatusEnum.SECRET.getValue());
            } else {
                TeamStatusEnum statusEnum = TeamStatusEnum.getEnumById(status);
                if (statusEnum == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不合法");
                }
                if (!isAdmin && statusEnum == TeamStatusEnum.PRIVATE) {
                    throw new BusinessException(ErrorCode.NO_AUTH, "无权限查询私有队伍");
                }
                queryWrapper.eq("status", status);
            }
        }
        //不展示已过期的队伍
        //expire_time is null or expire_time > now()
        //queryWrapper.and()
        queryWrapper.and(qw -> qw.isNull("expire_time").or().gt("expire_time", new Date()));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //TODO:关联查询用户信息
        //1.自己写sql
        //select * from team t left join user u on t.user_id = u.id 查询队伍和加入队伍成员的信息
        //查询队伍和已加入队伍的信息
        //select * from team t left join UserTeam ut on t.id = ut.team_id left join user u on ut.user_id = u.id
        // 关联查询创建人的用户信息
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            try {
                BeanUtils.copyProperties(teamUserVo, team);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询队伍参数转换失败");
            }
            UserVo userVo = new UserVo();
            try {
                BeanUtils.copyProperties(userVo, user);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询队伍参数转换失败");
            }
            teamUserVo.setCreateUser(userVo);
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    /**
     * 更新队伍信息
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null || teamUpdateRequest.getId() == null || teamUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍信息为空或id不合法");
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不合法");
        }
        Team oldteam = this.getById(teamUpdateRequest.getId());
        //判断老队伍是否存在
        if (oldteam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //判断用户是否有权限更新队伍
        if (!oldteam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限更新该队伍");
        }
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumById(teamUpdateRequest.getStatus());
        if(teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不合法");
        }
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET) && StringUtils.isBlank(teamUpdateRequest.getPassword())) {
            //如果老队伍是加密状态且有密码,且新传入的队伍状态是加密状态且没有传入密码,则使用老队伍的密码
            if (StringUtils.isNotBlank(oldteam.getPassword()) && oldteam.getStatus().equals(TeamStatusEnum.SECRET.getValue())) {
                teamUpdateRequest.setPassword(oldteam.getPassword());
            } else {
                //否则返回错误信息，加密队伍必须有密码
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍必须设置密码");
            }
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamUpdateRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍信息转换失败");
        }
        if (team == null || team.getId() == null || team.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍信息为空或id不合法");
        }
        return this.updateById(team);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        //Todo:加锁防止用户重复加入队伍
        if (teamJoinRequest == null || teamJoinRequest.getTeamId() == null || teamJoinRequest.getTeamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍信息为空或id不合法");
        }
        long userId = loginUser.getId();
        Team team = this.getById(teamJoinRequest.getTeamId());
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        //判断队伍是否过期
        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumById(status);
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不合法");
        }
        //判断队伍是否为私有状态
        if (status.equals(TeamStatusEnum.PRIVATE.getValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍为私有状态，无法加入");
        }
        //判断队伍是否为加密状态
        if (status.equals(TeamStatusEnum.SECRET.getValue())) {
            String password = teamJoinRequest.getPassword();
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不正确");
            }
        }
        //判断队伍是否已满
        long hasJoinUserCount = countTeamUserByTeamId(team.getId());
        if (hasJoinUserCount >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
        }
        //判断用户是否已经到达队伍上限
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long hasJoinCount = userTeamService.count(queryWrapper);
        if (hasJoinCount >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建和加入 5 个队伍");
        }
        //不能重复加入同一个队伍
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("team_id", team.getId());
        if (userTeamService.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
        }
        //修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null || teamQuitRequest.getTeamId() == null || teamQuitRequest.getTeamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍信息为空或id不合法");
        }
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不合法");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //判断用户是否加入该队伍
        long userId = loginUser.getId();
        UserTeam queryTeam = new UserTeam();
        queryTeam.setUserId(userId);
        queryTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryTeam);
        long count = userTeamService.count(queryWrapper);
        if (count <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未加入该队伍");
        }
        long teamUserCount = countTeamUserByTeamId(teamId);
        //如果队伍只有一个人，直接解散队伍
        if (teamUserCount <= 1) {
            //删除队伍和所有队伍的关系
            this.removeById(teamId);
        } else {
            //如果队伍有多人，判断是否是队长
            if (team.getUserId().equals(userId)) {
                //如果是队长，转让队长给最早加入的用户
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("team_id", teamId);
                //只需要取前两条数据`
                userTeamQueryWrapper.orderByAsc("join_time");
                userTeamQueryWrapper.last("LIMIT 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() == 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
                }
                UserTeam newLeader = userTeamList.get(1);
                Long nextLeaderId = newLeader.getUserId();
                Team newTeam = new Team();
                newTeam.setId(teamId);
                newTeam.setUserId(nextLeaderId);
                boolean result = this.updateById(newTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队长失败");
                }
            }
        }
        //删除用户和队伍的关系
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
        }
        return true;
    }

    /**
     * 删除队伍
     *
     * @param id
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTeam(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不合法");
        }
        Team team = this.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (!team.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限删除该队伍");
        }
        //删除队伍和所有队伍的关系
        boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", id);
        boolean result1 = userTeamService.remove(queryWrapper);
        if (!result1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        return true;
    }

    /**
     * 获取我加入的队伍列表
     *
     * @param loginUser
     * @return
     */
    @Override
    public List<TeamUserVo> getMyJoinTeamList(User loginUser) {
        Long id = loginUser.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不存在或id不合法");
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        List<TeamUserVo> teamUserVos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userTeamList)) {
            List<Long> teamIds = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
            QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
            teamQueryWrapper.in("id", teamIds);
            teamQueryWrapper.and(qw -> qw.isNull("expire_time").or().gt("expire_time", new Date()));
            List<Team> teamList = this.list(teamQueryWrapper);
            for (Team team : teamList) {
                TeamUserVo teamUserVo = new TeamUserVo();
                try {
                    BeanUtils.copyProperties(teamUserVo, team);
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询队伍参数转换失败");
                }
                User user = userService.getById(team.getUserId());
                UserVo userVo = new UserVo();
                if (user != null) {
                    try {
                        BeanUtils.copyProperties(userVo, user);
                    } catch (Exception e) {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询队伍参数转换失败");
                    }
                    teamUserVo.setCreateUser(userVo);
                }
                teamUserVos.add(teamUserVo);
            }
        }
        return teamUserVos;
    }

    /**
     * 根据队伍id获取队伍成员列表
     * @param teamSearchUserRequest
     * @param loginUser
     * @return
     */
    @Override
    public Page<UserVo> getUserListInTeam(TeamSearchUserRequest teamSearchUserRequest, User loginUser) {
        if (teamSearchUserRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long teamId = teamSearchUserRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不存在或id不合法");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "数据出错，查看的队伍不存在");
        }

        // 1. 权限校验（满足任一：是管理员 / 是队伍成员，直接放行）
        boolean isMemberOrAdmin = userService.isAdmin(loginUser) || isUserInTeam(teamId, loginUser.getId());
        if (!isMemberOrAdmin) {
            // 2. 队伍状态校验
            Integer status = getStatus(team);
            // 加密队伍：非成员需校验密码
            if (TeamStatusEnum.SECRET.getValue() == status) {
                String password = teamSearchUserRequest.getPassword();
                if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不正确，无权查看");
                }
            }
        }

        // 2. 对关联表 user_team 进行分页查询
        Integer pageNum = teamSearchUserRequest.getPageNum();
        Integer pageSize = teamSearchUserRequest.getPageSize();

        if (pageNum == null || pageSize == null
                || pageNum <= 0 || pageSize <= 0 || pageSize > 50) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR, "分页参数不合法");
        }

        Page<UserTeam> userTeamPage = userTeamService.lambdaQuery()
                .inSql(UserTeam::getUserId,
                        "SELECT id FROM `user` WHERE is_delete = 0")
                .eq(UserTeam::getTeamId, teamId)
                .orderByAsc(UserTeam::getId)
                .page(new Page<>(pageNum, pageSize));

        List<UserTeam> userTeamList = userTeamPage.getRecords();

        // 初始化返回的分页容器（保留 total、current、size 等分页元数据）
        Page<UserVo> userVoPage = new Page<>(pageNum, pageSize, userTeamPage.getTotal());
        if (CollectionUtils.isEmpty(userTeamList)) {
            return userVoPage.setRecords(Collections.emptyList());
        }

        // 3. 批量查询用户信息并映射回原分页顺序
        List<Long> userIds = userTeamList.stream()
                .map(UserTeam::getUserId)
                .collect(Collectors.toList());

        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (k1, k2) -> k1));

        // 组装 UserVo 列表（按 user_team 的分页顺序输出）
        List<UserVo> userVoList = userIds.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(user -> {
                    UserVo userVo = new UserVo();
                    try {
                        BeanUtils.copyProperties(userVo, user);
                    } catch (Exception e) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据转换错误");
                    }
                    return userVo;
                })
                .collect(Collectors.toList());

        return userVoPage.setRecords(userVoList);
    }

    /**
     * 获取队伍状态并校验权限
     * @param team
     * @return
     */
    private static @NonNull Integer getStatus(Team team) {
        Integer status = team.getStatus();
        //判断队伍状态是否合法
        List<Integer> statusList = Arrays.asList(TeamStatusEnum.PUBLIC.getValue(), TeamStatusEnum.SECRET.getValue(), TeamStatusEnum.PRIVATE.getValue());
        if(!statusList.contains(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不合法");
        }
        // 私有队伍：非成员/管理员禁止查看
        if (TeamStatusEnum.PRIVATE.getValue() == status) {
            throw new BusinessException(ErrorCode.NO_AUTH, "私有队伍无权查看成员");
        }
        return status;
    }

    /**
     * 查询队伍人数
     *
     * @param teamId
     * @return
     */
    public long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        return userTeamService.count(queryWrapper);
    }

    /**
     * 根据id判断用户是否在队伍中
     * @param teamId
     * @param userId
     * @return
     */
    public Boolean isUserInTeam(Long teamId, Long userId) {
        if (teamId == null || teamId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id或用户id不合法");
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        queryWrapper.eq("user_id", userId);
        long count = userTeamService.count(queryWrapper);
        return count > 0;
    }
}




