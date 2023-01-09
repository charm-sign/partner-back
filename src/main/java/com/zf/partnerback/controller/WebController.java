package com.zf.partnerback.controller;

import com.zf.partnerback.common.Result;
import com.zf.partnerback.entity.User;

import com.zf.partnerback.entity.domain.DTO.LoginDTO;
import com.zf.partnerback.entity.domain.DTO.UserRequest;
import com.zf.partnerback.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result login(@RequestBody User user) {
        long start = System.currentTimeMillis();
        LoginDTO res = userService.login(user);
        log.info("登录成功，耗时：{}ms", System.currentTimeMillis() - start);
        return Result.success(res);

    }

    @ApiOperation(value = "用户注册接口")
    @PostMapping(value = "/register")
    public Result register(@RequestBody UserRequest user) {
        userService.register(user);
        return Result.success();
    }

    @ApiOperation(value = "邮件发送接口")
    @GetMapping("/email")
    public Result sendEmail(@RequestParam String email, @RequestParam String type) {//@RequestParam 接收路径?**&***拼接方式
        userService.sendEmail(email, type);
        return Result.success();
    }

    @ApiOperation(value = "重置密码")
    @PostMapping("/password/reset")
    public Result resetPassword(@RequestBody UserRequest userRequest) {//@RequestParam 接收路径?**&***拼接方式
        String newPass = userService.resetPassword(userRequest);
        return Result.success(newPass);
    }
}
