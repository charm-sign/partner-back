package com.zf.partnerback;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@Slf4j
@SpringBootApplication
@MapperScan("com.zf.partnerback.mapper")
public class PartnerBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerBackApplication.class, args);
        log.info("Partner项目启动成功...");
    }

}
