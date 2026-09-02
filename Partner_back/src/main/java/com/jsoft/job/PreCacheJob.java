package com.jsoft.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jsoft.pojo.User;
import com.jsoft.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热机制
 *
 * @Author: F4EN
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserService userService;
    //引入RedissonClient
    @Resource
    private RedissonClient redissonClient;

    //重点用户
    List<Long> mainUserList = Arrays.asList(1L);

    @Scheduled(cron = "55 55 12 * * ?") // 每天执行 在12：55：55
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("lock:cache:recommend:user");
        try {
            //只有一个线程能获取锁
            if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                // 实现缓存预热逻辑
                for (Long userId : mainUserList) {
                    //查数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> page = userService.page(new Page<>(1, 10), queryWrapper);
                    // 构造Redis key
                    String redisKey = String.format("user:recommend:%s", mainUserList);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        valueOperations.set(redisKey, page, 60, TimeUnit.MINUTES);
                        log.info("缓存预热成功，key为:{}", redisKey);
                    } catch (Exception e) {
                        log.error("缓存预热失败，原因是:{}", e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("缓存分布式锁错误，原因是:{}", e.getMessage());
        }finally {
            //只能自己释放自己设置的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
