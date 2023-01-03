package com.zf.partnerback;

import com.zf.partnerback.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PartnerBackApplicationTests {
@Autowired
    IUserService userService;
    @Test
    void contextLoads() {
        //逻辑删除测试
        userService.removeById(3);
        System.out.println(userService.getById(3));
    }

}
