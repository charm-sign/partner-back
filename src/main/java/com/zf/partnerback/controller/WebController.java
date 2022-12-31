package com.zf.partnerback.controller;

import com.zf.partnerback.common.Result;
import com.zf.partnerback.entity.User;
import com.zf.partnerback.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: WebController
 * @Description: 无需权限即可访问的接口
 * @Author: ZF
 * @date: 2022/12/22 17:53
 * http://localhost:9090/swagger-ui/index.html
 */
@Api(tags = "无权限接口列表")
@RestController
@Slf4j
public class WebController {
    @Autowired
    private IUserService userService;

    @ApiOperation(value = "版本校验接口")
    @GetMapping(value = "/")
    public String version() {
        String ver = "partner-back-0.0.1-SNAPSHOT";  // 应用版本号
        Package aPackage = WebController.class.getPackage();
        String title = aPackage.getImplementationTitle();
        String version = aPackage.getImplementationVersion();
        if (title != null && version != null) {
            ver = String.join("-", title, version);
        }
        return ver;
    }


    @ApiOperation(value = "用户登录接口")
    @PostMapping(value = "/login")
    public Result login(@RequestBody User user){

       User res= userService.login(user);
        return Result.success(res);
    }
}
