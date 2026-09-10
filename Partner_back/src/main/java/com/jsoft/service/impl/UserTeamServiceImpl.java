package com.jsoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jsoft.Common.ErrorCode;
import com.jsoft.exception.BusinessException;
import com.jsoft.pojo.Dto.UserVo;
import com.jsoft.pojo.entity.User;
import com.jsoft.pojo.entity.UserTeam;
import com.jsoft.service.UserService;
import com.jsoft.service.UserTeamService;
import com.jsoft.mapper.UserTeamMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2026-09-06 15:56:16
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements UserTeamService{
    @Resource
    private UserService userService;
    /**
     * 根据id分页获取队伍成员列表
     * @param teamId
     * @param page
     * @return
     */
    @Override
    public List<UserVo> getUserListByTeamId(Long teamId, Page<UserVo> page) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        Page<UserTeam> userTeamPage = this.page(new Page<>(page.getCurrent(), page.getSize()), queryWrapper);

        List<Long> userIds = userTeamPage.getRecords().stream()
                .map(UserTeam::getUserId)
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> userList = userService.listByIds(userIds);
        List<UserVo> userVoList = new ArrayList<>();
        for (User user : userList) {
            UserVo userVo = new UserVo();
            try {
                BeanUtils.copyProperties(userVo, user);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息转换失败");
            }
            userVoList.add(userVo);
        }
        return userVoList;
    }
}




