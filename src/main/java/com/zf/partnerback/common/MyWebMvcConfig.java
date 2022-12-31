package com.zf.partnerback.common;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @ClassName: MyWebMvcConfig
 * @Description: 资源映射
 * @Author: ZF
 * @date: 2022/12/22 17:18
 */
@Configuration
public class MyWebMvcConfig extends WebMvcConfigurationSupport {
//    @Override
//    protected void addInterceptors(InterceptorRegistry registry) {
//        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
//        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
//                .addPathPatterns("/**")
//                .excludePathPatterns("/", "/login", "/register", "/email", "/password/reset", "/file/download/**")
//                .excludePathPatterns("/swagger**/**", "/webjars/**", "/v3/**", "/doc.html", "/favicon.ico");  // 排除 swagger拦截
//    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.
                addResourceHandler("/swagger-ui/**")//将此目录下的所有资源映射到下面这个目录**/index.html，才能测试接口
                //所以访问swagger的路径就是http://localhost:9090/swagger-ui/index.html
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }

    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html");
    }
}

