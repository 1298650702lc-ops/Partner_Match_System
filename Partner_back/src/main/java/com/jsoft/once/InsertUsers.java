package com.jsoft.once;
import java.util.Date;

import com.jsoft.mapper.UserMapper;
import com.jsoft.pojo.User;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    //@Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void insertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int InsertNum = 100000;
        for (int i = 0; i < InsertNum; i++) {
            User user = new User();
            user.setUsername("假用户" + i);
            user.setUserAccount("fakeUser" + i);
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("151434448965");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("1111");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println("插入用户耗时: " + stopWatch.getTime() + " ms");
    }

}