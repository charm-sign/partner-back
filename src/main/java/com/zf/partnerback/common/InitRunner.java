package com.zf.partnerback.common;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import com.zf.partnerback.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @ClassName: InitRunner
 * @Description: 手动连接数据库，使其初始化，提高登录效率
 * @Author: ZF
 * @date: 2023/1/3 18:13
 */
@Component
@Slf4j
public class InitRunner implements ApplicationRunner { //项目启动就执行这个里面的功能
    @Autowired
    IUserService userService;

    /**
     * 项目启动成功之后就会执行此方法，达到优化登录目的。
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
//发送一次异步web请求，初始化tomcat连接
        ThreadUtil.execAsync(()->{
            userService.select1();
            log.info("手动初始化数据库成功");
            HttpUtil.get("http://localhost:9090");
            log.info("模拟网络请求实现tomcat连接优化");
        });
    }
}
